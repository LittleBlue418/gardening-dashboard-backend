# Scala Backend with MongoDB for Gardening Dashboard

## Context

Currently, the gardening dashboard is a Vue.js frontend application that stores all data in static JSON files (`homeData.json`, `gardenData.json`, etc.) and uses browser localStorage for task completion states. This works for a single user but has limitations:

- No data persistence across devices
- No sharing or collaboration features
- Task states are stored per-browser, not per-user
- No ability to add/edit data dynamically

**Goal:** Build a Scala backend using Play Framework and MongoDB Atlas to enable multi-user support with OAuth authentication, starting with a minimal migration of task completion states.

**Selected Architecture:**
- **Framework:** Play Framework (full-featured, great for learning Scala web development)
- **Database:** MongoDB Atlas (cloud-hosted, free tier)
- **Auth:** OAuth/social login (Google OAuth 2.0)
- **Migration Strategy:** Gradual - start with task checkboxes API, keep JSON files for now

---

## Phase 1: Backend Setup & Infrastructure

### 1.1 Play Framework Project Structure

This project was created with:
```bash
sbt new playframework/play-scala-seed.g8
```

**Current structure:**
```
gardening-dashboard-backend/
├── app/
│   ├── controllers/         # API endpoints (to create)
│   ├── models/              # Case classes for domain models (to create)
│   ├── repositories/        # MongoDB data access layer (to create)
│   └── services/            # Business logic (to create)
├── conf/
│   ├── application.conf     # Configuration (DB, OAuth) - to modify
│   └── routes              # API route definitions - to modify
├── build.sbt               # Dependencies - to modify
└── test/                   # Unit/integration tests
```

**Key dependencies to add to `build.sbt`:**
```scala
libraryDependencies ++= Seq(
  guice,
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.11.0",
  "com.typesafe.play" %% "play-json" % "2.10.3",
  "org.playframework.silhouette" %% "play-silhouette" % "9.0.0",
  "org.playframework.silhouette" %% "play-silhouette-crypto-jca" % "9.0.0",
  "org.playframework.silhouette" %% "play-silhouette-password-bcrypt" % "9.0.0",
  "org.playframework.silhouette" %% "play-silhouette-persistence" % "9.0.0",
  "com.iheart" %% "ficus" % "1.5.2",
  "net.codingwell" %% "scala-guice" % "6.0.0"
)
```

**Why Silhouette?** It's the standard authentication library for Play Framework, with built-in OAuth support.

---

### 1.2 MongoDB Atlas Setup

1. **Create MongoDB Atlas account**: https://www.mongodb.com/cloud/atlas/register
2. **Create a free cluster** (M0 tier)
3. **Database name:** `gardening-dashboard`
4. **Initial collections:**
   - `users` - OAuth user profiles
   - `taskCompletions` - Checkbox states (phase 1)
   - (Future: `plants`, `plantingSchedules`, `beds`, `tasks`)

5. **Configure network access:**
   - Add IP whitelist: `0.0.0.0/0` (allow from anywhere - for development)
   - Later: restrict to deployment server IP

6. **Create database user:**
   - Username: `gardening-app`
   - Password: (generate secure password)
   - Permissions: `readWrite` on `gardening-dashboard` database

7. **Get connection string:**
   ```
   mongodb+srv://gardening-app:<password>@cluster0.xxxxx.mongodb.net/gardening-dashboard?retryWrites=true&w=majority
   ```

8. **Add to `conf/application.conf`:**
   ```hocon
   mongodb.uri = "mongodb+srv://gardening-app:<password>@cluster0.xxxxx.mongodb.net/gardening-dashboard"
   mongodb.uri = ${?MONGODB_URI}  # Override with env var in production
   ```

---

### 1.3 Google OAuth Setup

1. **Go to Google Cloud Console**: https://console.cloud.google.com/
2. **Create new project**: "Gardening Dashboard"
3. **Enable Google+ API** (for OAuth profile data)
4. **Create OAuth 2.0 credentials:**
   - Type: Web application
   - Authorized JavaScript origins: `http://localhost:5173` (Vite dev server)
   - Authorized redirect URIs: `http://localhost:9000/authenticate/google` (Play server)
   - Production URIs to add later

5. **Copy Client ID and Client Secret**

6. **Add to `conf/application.conf`:**
   ```hocon
   silhouette {
     google {
       authorizationURL = "https://accounts.google.com/o/oauth2/auth"
       accessTokenURL = "https://accounts.google.com/o/oauth2/token"
       redirectURL = "http://localhost:9000/authenticate/google"
       clientID = "your-client-id.apps.googleusercontent.com"
       clientID = ${?GOOGLE_CLIENT_ID}
       clientSecret = "your-client-secret"
       clientSecret = ${?GOOGLE_CLIENT_SECRET}
       scope = "profile email"
     }
   }
   
   play.filters.cors {
     allowedOrigins = ["http://localhost:5173"]
     allowedHttpMethods = ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
     allowedHttpHeaders = ["Accept", "Content-Type", "Authorization"]
     supportsCredentials = true
   }
   
   play.http.session.cookieName = "GARDENING_SESSION"
   play.http.session.secure = false  # true in production
   play.http.session.httpOnly = true
   play.http.session.sameSite = "lax"
   ```

---

## Phase 2: Core Backend Implementation

### 2.1 Domain Models

**File:** `app/models/User.scala`
```scala
package models

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._

case class User(
  _id: ObjectId = new ObjectId(),
  googleId: String,
  email: String,
  name: String,
  pictureUrl: Option[String],
  createdAt: Long = System.currentTimeMillis()
)

object User {
  implicit val objectIdFormat: Format[ObjectId] = new Format[ObjectId] {
    def reads(json: JsValue): JsResult[ObjectId] = json match {
      case JsString(s) => JsSuccess(new ObjectId(s))
      case _ => JsError("Expected ObjectId as string")
    }
    def writes(o: ObjectId): JsValue = JsString(o.toHexString)
  }
  
  implicit val format: OFormat[User] = Json.format[User]
}
```

**File:** `app/models/TaskCompletion.scala`
```scala
package models

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._

case class TaskCompletion(
  _id: ObjectId = new ObjectId(),
  userId: String,           // User's googleId
  taskId: String,           // e.g., "priority-0", "garden-1"
  completed: Boolean,
  lastModified: Long = System.currentTimeMillis()
)

object TaskCompletion {
  implicit val objectIdFormat: Format[ObjectId] = User.objectIdFormat
  implicit val format: OFormat[TaskCompletion] = Json.format[TaskCompletion]
}
```

---

### 2.2 MongoDB Repository Layer

**File:** `app/repositories/MongoConnection.scala`
```scala
package repositories

import com.google.inject.{Inject, Singleton}
import org.mongodb.scala.{MongoClient, MongoDatabase}
import play.api.Configuration

@Singleton
class MongoConnection @Inject()(config: Configuration) {
  private val mongoUri: String = config.get[String]("mongodb.uri")
  
  val client: MongoClient = MongoClient(mongoUri)
  val database: MongoDatabase = client.getDatabase("gardening-dashboard")
  
  def close(): Unit = client.close()
}
```

**File:** `app/repositories/UserRepository.scala`
```scala
package repositories

import com.google.inject.{Inject, Singleton}
import models.User
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject()(
  mongoConnection: MongoConnection
)(implicit ec: ExecutionContext) {
  
  private val collection: MongoCollection[Document] = 
    mongoConnection.database.getCollection("users")
  
  def findByGoogleId(googleId: String): Future[Option[User]] = {
    collection
      .find(equal("googleId", googleId))
      .first()
      .toFutureOption()
      .map(_.map(docToUser))
  }
  
  def findByEmail(email: String): Future[Option[User]] = {
    collection
      .find(equal("email", email))
      .first()
      .toFutureOption()
      .map(_.map(docToUser))
  }
  
  def create(user: User): Future[User] = {
    collection
      .insertOne(userToDoc(user))
      .toFuture()
      .map(_ => user)
  }
  
  def update(user: User): Future[Boolean] = {
    collection
      .replaceOne(equal("googleId", user.googleId), userToDoc(user))
      .toFuture()
      .map(_.wasAcknowledged())
  }
  
  private def userToDoc(user: User): Document = Document(
    "_id" -> user._id,
    "googleId" -> user.googleId,
    "email" -> user.email,
    "name" -> user.name,
    "pictureUrl" -> user.pictureUrl,
    "createdAt" -> user.createdAt
  )
  
  private def docToUser(doc: Document): User = {
    User(
      _id = doc.getObjectId("_id"),
      googleId = doc.getString("googleId"),
      email = doc.getString("email"),
      name = doc.getString("name"),
      pictureUrl = doc.get[String]("pictureUrl"),
      createdAt = doc.getLong("createdAt")
    )
  }
}
```

**File:** `app/repositories/TaskCompletionRepository.scala`
```scala
package repositories

import com.google.inject.{Inject, Singleton}
import models.TaskCompletion
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.ReplaceOptions
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaskCompletionRepository @Inject()(
  mongoConnection: MongoConnection
)(implicit ec: ExecutionContext) {
  
  private val collection: MongoCollection[Document] = 
    mongoConnection.database.getCollection("taskCompletions")
  
  def findByUser(userId: String): Future[Seq[TaskCompletion]] = {
    collection
      .find(equal("userId", userId))
      .toFuture()
      .map(_.map(docToTaskCompletion))
  }
  
  def findByUserAndTask(userId: String, taskId: String): Future[Option[TaskCompletion]] = {
    collection
      .find(and(equal("userId", userId), equal("taskId", taskId)))
      .first()
      .toFutureOption()
      .map(_.map(docToTaskCompletion))
  }
  
  def upsert(taskCompletion: TaskCompletion): Future[TaskCompletion] = {
    collection
      .replaceOne(
        and(
          equal("userId", taskCompletion.userId),
          equal("taskId", taskCompletion.taskId)
        ),
        taskCompletionToDoc(taskCompletion),
        ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => taskCompletion)
  }
  
  def delete(userId: String, taskId: String): Future[Boolean] = {
    collection
      .deleteOne(and(equal("userId", userId), equal("taskId", taskId)))
      .toFuture()
      .map(_.wasAcknowledged())
  }
  
  private def taskCompletionToDoc(tc: TaskCompletion): Document = Document(
    "_id" -> tc._id,
    "userId" -> tc.userId,
    "taskId" -> tc.taskId,
    "completed" -> tc.completed,
    "lastModified" -> tc.lastModified
  )
  
  private def docToTaskCompletion(doc: Document): TaskCompletion = {
    TaskCompletion(
      _id = doc.getObjectId("_id"),
      userId = doc.getString("userId"),
      taskId = doc.getString("taskId"),
      completed = doc.getBoolean("completed"),
      lastModified = doc.getLong("lastModified")
    )
  }
}
```

---

### 2.3 Authentication Service

**File:** `app/services/AuthService.scala`
```scala
package services

import com.google.inject.{Inject, Singleton}
import models.User
import repositories.UserRepository
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthService @Inject()(
  userRepository: UserRepository
)(implicit ec: ExecutionContext) {
  
  def findOrCreateUser(googleProfile: GoogleProfile): Future[User] = {
    userRepository.findByGoogleId(googleProfile.id).flatMap {
      case Some(user) => 
        // Update user info if changed
        val updatedUser = user.copy(
          email = googleProfile.email,
          name = googleProfile.name,
          pictureUrl = googleProfile.picture
        )
        if (updatedUser != user) {
          userRepository.update(updatedUser).map(_ => updatedUser)
        } else {
          Future.successful(user)
        }
      
      case None =>
        // Create new user
        val newUser = User(
          googleId = googleProfile.id,
          email = googleProfile.email,
          name = googleProfile.name,
          pictureUrl = googleProfile.picture
        )
        userRepository.create(newUser)
    }
  }
}

case class GoogleProfile(
  id: String,
  email: String,
  name: String,
  picture: Option[String]
)

object GoogleProfile {
  implicit val format = Json.format[GoogleProfile]
}
```

---

### 2.4 API Controllers

**File:** `app/controllers/AuthController.scala`
```scala
package controllers

import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import services.{AuthService, GoogleProfile}
import scala.concurrent.{ExecutionContext, Future}

class AuthController @Inject()(
  cc: ControllerComponents,
  ws: WSClient,
  authService: AuthService
)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  
  def googleCallback(code: String) = Action.async { implicit request =>
    // Exchange code for access token
    val tokenUrl = "https://oauth2.googleapis.com/token"
    val clientId = // from config
    val clientSecret = // from config
    val redirectUri = "http://localhost:9000/authenticate/google"
    
    ws.url(tokenUrl).post(Map(
      "code" -> Seq(code),
      "client_id" -> Seq(clientId),
      "client_secret" -> Seq(clientSecret),
      "redirect_uri" -> Seq(redirectUri),
      "grant_type" -> Seq("authorization_code")
    )).flatMap { tokenResponse =>
      val accessToken = (tokenResponse.json \ "access_token").as[String]
      
      // Fetch user profile
      ws.url("https://www.googleapis.com/oauth2/v2/userinfo")
        .withHttpHeaders("Authorization" -> s"Bearer $accessToken")
        .get()
        .flatMap { profileResponse =>
          val googleProfile = profileResponse.json.as[GoogleProfile]
          
          authService.findOrCreateUser(googleProfile).map { user =>
            Redirect("http://localhost:5173")
              .withSession("userId" -> user.googleId)
          }
        }
    }
  }
  
  def currentUser = Action.async { implicit request =>
    request.session.get("userId") match {
      case Some(userId) =>
        // Return user info
        Future.successful(Ok(Json.obj(
          "authenticated" -> true,
          "userId" -> userId
        )))
      
      case None =>
        Future.successful(Ok(Json.obj("authenticated" -> false)))
    }
  }
  
  def logout = Action {
    Redirect("http://localhost:5173").withNewSession
  }
}
```

**File:** `app/controllers/TaskCompletionController.scala`
```scala
package controllers

import com.google.inject.Inject
import models.TaskCompletion
import play.api.libs.json._
import play.api.mvc._
import repositories.TaskCompletionRepository
import scala.concurrent.{ExecutionContext, Future}

class TaskCompletionController @Inject()(
  cc: ControllerComponents,
  taskRepo: TaskCompletionRepository
)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  
  // GET /api/tasks/completions
  def getAll = Action.async { implicit request =>
    request.session.get("userId") match {
      case Some(userId) =>
        taskRepo.findByUser(userId).map { completions =>
          Ok(Json.toJson(completions))
        }
      
      case None =>
        Future.successful(Unauthorized(Json.obj("error" -> "Not authenticated")))
    }
  }
  
  // PUT /api/tasks/completions/:taskId
  def update(taskId: String) = Action.async(parse.json) { implicit request =>
    request.session.get("userId") match {
      case Some(userId) =>
        (request.body \ "completed").asOpt[Boolean] match {
          case Some(completed) =>
            val taskCompletion = TaskCompletion(
              userId = userId,
              taskId = taskId,
              completed = completed
            )
            
            taskRepo.upsert(taskCompletion).map { _ =>
              Ok(Json.toJson(taskCompletion))
            }
          
          case None =>
            Future.successful(BadRequest(Json.obj("error" -> "Missing 'completed' field")))
        }
      
      case None =>
        Future.successful(Unauthorized(Json.obj("error" -> "Not authenticated")))
    }
  }
  
  // DELETE /api/tasks/completions/:taskId
  def delete(taskId: String) = Action.async { implicit request =>
    request.session.get("userId") match {
      case Some(userId) =>
        taskRepo.delete(userId, taskId).map { _ =>
          NoContent
        }
      
      case None =>
        Future.successful(Unauthorized(Json.obj("error" -> "Not authenticated")))
    }
  }
}
```

---

### 2.5 Routes Configuration

**File:** `conf/routes`
```
# Authentication
GET     /authenticate/google             controllers.AuthController.googleCallback(code: String)
GET     /api/auth/me                     controllers.AuthController.currentUser
POST    /api/auth/logout                 controllers.AuthController.logout

# Task Completions
GET     /api/tasks/completions           controllers.TaskCompletionController.getAll
PUT     /api/tasks/completions/:taskId   controllers.TaskCompletionController.update(taskId: String)
DELETE  /api/tasks/completions/:taskId   controllers.TaskCompletionController.delete(taskId: String)

# Static JSON files (temporary - phase 1)
GET     /api/data/home                   controllers.DataController.homeData
GET     /api/data/garden                 controllers.DataController.gardenData
GET     /api/data/calendar               controllers.DataController.calendarData
GET     /api/data/plan                   controllers.DataController.planData
GET     /api/data/quickStats             controllers.DataController.quickStats
```

---

## Phase 3: Frontend Integration

**Note:** Frontend code lives in the sibling project `../gardening-dashboard/`

### 3.1 Environment Configuration

**File:** `../gardening-dashboard/.env.local` (create this file)
```bash
VITE_API_BASE_URL=http://localhost:9000/api
VITE_GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
VITE_OAUTH_REDIRECT_URI=http://localhost:9000/authenticate/google
```

**Add to `../gardening-dashboard/.gitignore`:**
```
.env.local
```

---

### 3.2 Create API Service Layer

**File:** `../gardening-dashboard/src/services/api.ts` (new file)
```typescript
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9000/api'

export interface TaskCompletion {
  userId: string
  taskId: string
  completed: boolean
  lastModified: number
}

export interface AuthStatus {
  authenticated: boolean
  userId?: string
}

// Auth API
export async function checkAuthStatus(): Promise<AuthStatus> {
  const response = await fetch(`${API_BASE_URL}/auth/me`, {
    credentials: 'include'
  })
  return response.json()
}

export function loginWithGoogle(): void {
  const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID
  const redirectUri = import.meta.env.VITE_OAUTH_REDIRECT_URI
  const scope = 'profile email'
  
  const authUrl = `https://accounts.google.com/o/oauth2/v2/auth?` +
    `client_id=${clientId}&` +
    `redirect_uri=${encodeURIComponent(redirectUri)}&` +
    `response_type=code&` +
    `scope=${encodeURIComponent(scope)}`
  
  window.location.href = authUrl
}

export async function logout(): Promise<void> {
  await fetch(`${API_BASE_URL}/auth/logout`, {
    method: 'POST',
    credentials: 'include'
  })
}

// Task Completions API
export async function getTaskCompletions(): Promise<TaskCompletion[]> {
  const response = await fetch(`${API_BASE_URL}/tasks/completions`, {
    credentials: 'include'
  })
  
  if (!response.ok) {
    throw new Error('Failed to fetch task completions')
  }
  
  return response.json()
}

export async function updateTaskCompletion(
  taskId: string,
  completed: boolean
): Promise<TaskCompletion> {
  const response = await fetch(`${API_BASE_URL}/tasks/completions/${taskId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    body: JSON.stringify({ completed })
  })
  
  if (!response.ok) {
    throw new Error('Failed to update task completion')
  }
  
  return response.json()
}
```

---

### 3.3 Update HomeTab.vue to Use API

**File:** `../gardening-dashboard/src/tabs/HomeTab.vue`

**Changes needed:**
1. Import the API service
2. Load task completions from API on mount (if authenticated)
3. Fall back to localStorage if not authenticated
4. Update `saveCheckbox` to call the API

**Modified script section:**
```typescript
import { ref, computed, onMounted } from 'vue'
import homeDataRaw from '../data/homeData.json'
import Section from '../components/Section.vue'
import { getTaskCompletions, updateTaskCompletion, checkAuthStatus } from '../services/api'

// ... existing interfaces ...

const homeData = homeDataRaw as HomeData
const checkboxes = ref<Checkboxes>({ /* ... */ })
const isAuthenticated = ref(false)

const saveCheckbox = async (id: string) => {
  const key = id.replace('-', '')
  const value = checkboxes.value[key as keyof Checkboxes]
  
  if (isAuthenticated.value) {
    // Save to API
    try {
      await updateTaskCompletion(id, value)
    } catch (error) {
      console.error('Failed to save to API:', error)
      // Fall back to localStorage
      localStorage.setItem(`checkbox-${id}`, String(value))
    }
  } else {
    // Save to localStorage
    localStorage.setItem(`checkbox-${id}`, String(value))
  }
}

const loadCheckboxes = async () => {
  // Check if user is authenticated
  const authStatus = await checkAuthStatus()
  isAuthenticated.value = authStatus.authenticated
  
  if (isAuthenticated.value) {
    // Load from API
    try {
      const completions = await getTaskCompletions()
      
      completions.forEach((completion) => {
        const key = completion.taskId.replace('-', '')
        if (key in checkboxes.value) {
          checkboxes.value[key as keyof Checkboxes] = completion.completed
        }
      })
    } catch (error) {
      console.error('Failed to load from API:', error)
      loadFromLocalStorage()
    }
  } else {
    loadFromLocalStorage()
  }
}

const loadFromLocalStorage = () => {
  const keys = Object.keys(checkboxes.value) as string[]
  keys.forEach((key) => {
    const id = key.replace(/(\d+)$/, '-$1')
    const saved = localStorage.getItem(`checkbox-${id}`)
    if (saved === 'true') {
      checkboxes.value[key] = true
    }
  })
}

onMounted(() => {
  loadCheckboxes()
})
```

---

### 3.4 Add Login Component (Optional for Phase 1)

**File:** `../gardening-dashboard/src/components/LoginButton.vue` (new file)
```vue
<template>
  <div class="login-container">
    <button v-if="!isAuthenticated" @click="handleLogin" class="login-btn">
      Sign in with Google
    </button>
    <div v-else class="user-info">
      <span>{{ userName }}</span>
      <button @click="handleLogout" class="logout-btn">Logout</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { loginWithGoogle, logout, checkAuthStatus } from '../services/api'

const isAuthenticated = ref(false)
const userName = ref('')

const handleLogin = () => {
  loginWithGoogle()
}

const handleLogout = async () => {
  await logout()
  isAuthenticated.value = false
  window.location.reload()
}

const checkAuth = async () => {
  const status = await checkAuthStatus()
  isAuthenticated.value = status.authenticated
  if (status.authenticated && status.userId) {
    userName.value = status.userId // Could fetch full user name
  }
}

onMounted(() => {
  checkAuth()
})
</script>

<style scoped>
.login-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.login-btn {
  background: #4285f4;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.login-btn:hover {
  background: #357ae8;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logout-btn {
  background: #f44336;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.logout-btn:hover {
  background: #da190b;
}
</style>
```

You can add this to `App.vue` in the header area.

---

## Phase 4: Deployment & Testing

### 4.1 Local Development Setup

**Terminal 1 - Start Scala backend (this project):**
```bash
cd /Users/holly.thomas@m10s.io/gardening-dashboard-backend/gardening-dashboard-backend
sbt run
# Runs on http://localhost:9000
```

**Terminal 2 - Start Vue frontend:**
```bash
cd /Users/holly.thomas@m10s.io/gardening-dashboard
npm run dev
# Runs on http://localhost:5173
```

---

### 4.2 Testing Checklist

**Backend API Tests:**
- [ ] GET `/api/auth/me` returns authentication status
- [ ] Google OAuth callback redirects correctly
- [ ] Session cookie is set after authentication
- [ ] GET `/api/tasks/completions` returns empty array for new user
- [ ] PUT `/api/tasks/completions/priority-0` creates new completion
- [ ] GET `/api/tasks/completions` returns the created completion
- [ ] PUT with same taskId updates existing completion (upsert)
- [ ] DELETE `/api/tasks/completions/priority-0` removes completion
- [ ] Unauthorized requests return 401

**Frontend Integration Tests:**
- [ ] Login button appears when not authenticated
- [ ] Clicking login redirects to Google
- [ ] After OAuth, user returns to dashboard
- [ ] Checking a task checkbox saves to API
- [ ] Refreshing page loads checkboxes from API
- [ ] Logout clears session and resets to localStorage

**Database Verification:**
```bash
# Connect to MongoDB Atlas using MongoDB Compass or CLI
mongosh "mongodb+srv://cluster0.xxxxx.mongodb.net/gardening-dashboard" --username gardening-app

# Check collections
use gardening-dashboard
db.users.find()
db.taskCompletions.find()
```

---

### 4.3 Migration Strategy from localStorage

For existing users with localStorage data:

**Option 1: Manual migration button**
Add a "Sync to Cloud" button that:
1. Prompts user to login
2. Reads all localStorage checkbox states
3. Bulk uploads to API
4. Shows confirmation

**Option 2: Automatic migration**
On first login after API is deployed:
1. Check if localStorage has data
2. Check if API has data for this user
3. If API is empty but localStorage has data, auto-upload
4. Show notification: "Your tasks have been synced to the cloud!"

---

## Future Phases (Not in Scope for Phase 1)

**Phase 2:** Move static JSON data to MongoDB
- Migrate `gardenData.json` → `plants` collection
- Migrate `gardenPlanData.json` → `monthlyTasks` collection
- Create API endpoints for CRUD operations
- Add admin UI to edit planting schedule

**Phase 3:** Refactor calendar data structure
- Separate presentation (CSS positioning) from domain data
- Store crop schedules with start/end dates
- Calculate CSS positioning in frontend
- Add drag-and-drop timeline editor

**Phase 4:** Advanced features
- Multi-garden support (one user, multiple garden locations)
- Sharing/collaboration (multiple users per garden)
- Mobile app (React Native or Flutter)
- Weather API integration
- Push notifications for planting reminders

---

## Learning Opportunities

This project will teach you:

✅ **Scala fundamentals:**
- Case classes and pattern matching
- Futures and async programming
- Type-safe JSON serialization with Play JSON
- Dependency injection with Guice

✅ **Backend architecture:**
- Repository pattern for data access
- Service layer for business logic
- RESTful API design
- Session management and authentication

✅ **MongoDB:**
- Document-based data modeling
- CRUD operations with Scala driver
- Upsert patterns
- Indexing for performance

✅ **OAuth 2.0:**
- Authorization code flow
- Token exchange
- Session cookies vs JWTs
- CORS configuration

✅ **Full-stack integration:**
- API versioning
- Error handling
- State synchronization (API vs localStorage)
- Progressive enhancement (works offline with localStorage fallback)

---

## Estimated Effort

- **Backend setup (Play + MongoDB):** 3-4 hours
- **Google OAuth integration:** 2-3 hours
- **Task completion API:** 2 hours
- **Frontend integration:** 2-3 hours
- **Testing & debugging:** 2-3 hours

**Total:** ~12-15 hours for Phase 1

This is ambitious but achievable! The gradual migration strategy means you'll have a working system at each step.
