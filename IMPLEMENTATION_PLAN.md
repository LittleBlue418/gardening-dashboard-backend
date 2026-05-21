# Holly's notes

each folder = one responsibility
```
app/
├── controllers/    # HTTP layer
├── services/       # Business logic
├── repositories/   # Data access
├── models/         # Domain models
└── utils/          # Helper functions
```

# Scala Backend Implementation Plan - Gardening Dashboard

## Project Overview

Building a Scala backend using Play Framework and MongoDB Atlas to enable multi-user support with OAuth authentication for the gardening dashboard.

**Stack:**
- **Framework:** Play Framework 2.9+
- **Database:** MongoDB Atlas (cloud-hosted)
- **Language:** Scala 2.13
- **Auth:** Google OAuth 2.0

**Migration Strategy:** Gradual - start with task checkboxes API, then migrate static JSON data to MongoDB.

---

## ✅ Completed

- [x] Play Framework project setup (`sbt new playframework/play-scala-seed.g8`)
- [x] Dependencies configured in `build.sbt` (MongoDB driver, Play JSON, Guice)
- [x] Domain models created in `app/models/`:
  - `User.scala` - OAuth user profiles
  - `ToDoItem.scala` - Task items
  - `Plant.scala`, `Bed.scala`, `PlantingPlan.scala` - Garden domain models
  - `Calendar.scala`, `PlantingRecordYear.scala` - Calendar/scheduling models
  - `PlantingTypes.scala` - Enums and type definitions
  - `package.scala` - JSON formatters
- [x] Repository Layer (Phase 1):
  - `MongoConnection.scala` - MongoDB connection singleton
  - `UserRepository.scala` - Complete CRUD for User collection (findByGoogleId, findByEmail, create, update)

---

## 🚀 Next Steps

### 1. Repository Layer (Data Access) - Continued

#### `TaskCompletionRepository.scala`
CRUD operations for task completion states:
- `findByUser(userId: String): Future[Seq[TaskCompletion]]`
- `findByUserAndTask(userId: String, taskId: String): Future[Option[TaskCompletion]]`
- `upsert(taskCompletion: TaskCompletion): Future[TaskCompletion]`
- `delete(userId: String, taskId: String): Future[Boolean]`

**Note:** `upsert` = update if exists, insert if new (critical for checkboxes!)

**Pattern to follow:** Same as UserRepository - helper methods for conversion (`taskCompletionToDoc`, `docToTaskCompletion`) and async CRUD operations.

---

### 2. Service Layer (Business Logic)

Create `app/services/` directory with business logic classes.

**Files to create:**

#### `AuthService.scala`

Handle OAuth user management:
- `findOrCreateUser(googleProfile: GoogleProfile): Future[User]`

**Responsibilities:**
- Check if user exists by `googleId`
- If exists: update email/name/picture if changed
- If new: create user record
- Return the User object

**Why a service layer?**
Controllers should be thin - just HTTP request/response handling. Business logic (like "find or create") belongs in services. This makes the logic reusable and testable.

---

### 3. Controllers (API Endpoints)

Create controller classes in `app/controllers/` to handle HTTP requests.

**Files to create:**

#### `AuthController.scala`

Handle OAuth flow and session management.

**Endpoints needed:**
- `GET /authenticate/google?code=...` - OAuth callback from Google
  - Exchange auth code for access token
  - Fetch user profile from Google
  - Call `authService.findOrCreateUser()`
  - Set session cookie with userId
  - Redirect to frontend (`http://localhost:5173`)
  
- `GET /api/auth/me` - Check current authentication status
  - Return `{ authenticated: true/false, userId: "..." }`
  
- `POST /api/auth/logout` - Clear session
  - Clear session cookie
  - Redirect to frontend

**Dependencies to inject:**
- `WSClient` - for HTTP requests to Google APIs
- `AuthService` - business logic
- `Configuration` - to read OAuth credentials

---

#### `TaskCompletionController.scala`
Handle CRUD operations for task checkbox states.

**Endpoints needed:**
- `GET /api/tasks/completions` - Get all completions for current user
  - Check session for userId
  - Call `taskRepo.findByUser(userId)`
  - Return JSON array of completions

- `PUT /api/tasks/completions/:taskId` - Update/create completion
  - Check session for userId
  - Parse JSON body: `{ "completed": true }`
  - Call `taskRepo.upsert(TaskCompletion(...))`
  - Return updated completion

- `DELETE /api/tasks/completions/:taskId` - Delete completion
  - Check session for userId
  - Call `taskRepo.delete(userId, taskId)`
  - Return 204 No Content

**Security:** All endpoints must check `request.session.get("userId")` - return 401 Unauthorized if not present.

---

### 4. Routes Configuration

Update `conf/routes` file to wire HTTP endpoints to controller methods.

**Routes to add:**
```
# Authentication
GET     /authenticate/google             controllers.AuthController.googleCallback(code: String)
GET     /api/auth/me                     controllers.AuthController.currentUser
POST    /api/auth/logout                 controllers.AuthController.logout

# Task Completions
GET     /api/tasks/completions           controllers.TaskCompletionController.getAll
PUT     /api/tasks/completions/:taskId   controllers.TaskCompletionController.update(taskId: String)
DELETE  /api/tasks/completions/:taskId   controllers.TaskCompletionController.delete(taskId: String)
```

**Note:** Keep the existing `HomeController.index` route for now (Play Framework default).

---

## 🧪 Testing & Configuration

### MongoDB Atlas Setup

Before testing, ensure MongoDB is configured:

1. **Create MongoDB Atlas account** (if not done)
2. **Create free cluster** (M0 tier)
3. **Database name:** `gardening-dashboard`
4. **Collections:** `users`, `taskCompletions`
5. **Network access:** Add `0.0.0.0/0` (development only)
6. **Database user:** Create with `readWrite` permissions
7. **Get connection string** and add to `conf/application.conf`:

```hocon
mongodb.uri = "mongodb+srv://username:password@cluster.mongodb.net/gardening-dashboard"
mongodb.uri = ${?MONGODB_URI}  # Override with env var
```

### Google OAuth Setup

1. **Google Cloud Console:** Create project
2. **Enable Google+ API**
3. **Create OAuth 2.0 credentials:**
   - Authorized redirect URI: `http://localhost:9000/authenticate/google`
4. **Add to `conf/application.conf`:**

```hocon
google.clientId = "your-client-id.apps.googleusercontent.com"
google.clientId = ${?GOOGLE_CLIENT_ID}
google.clientSecret = "your-client-secret"
google.clientSecret = ${?GOOGLE_CLIENT_SECRET}

play.filters.cors {
  allowedOrigins = ["http://localhost:5173"]
  allowedHttpMethods = ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
  allowedHttpHeaders = ["Accept", "Content-Type", "Authorization"]
  supportsCredentials = true
}
```

### Local Development

**Start the backend:**
```bash
sbt run
# Runs on http://localhost:9000
```

### Backend API Testing Checklist

Test with `curl` or Postman:

- [ ] `GET /api/auth/me` returns `{ "authenticated": false }`
- [ ] Google OAuth flow redirects correctly
- [ ] After OAuth: session cookie is set
- [ ] `GET /api/auth/me` returns `{ "authenticated": true, "userId": "..." }`
- [ ] `GET /api/tasks/completions` returns empty array for new user
- [ ] `PUT /api/tasks/completions/test-task` with `{ "completed": true }` creates record
- [ ] `GET /api/tasks/completions` returns the created completion
- [ ] `PUT` with same taskId updates (upsert behavior)
- [ ] `DELETE /api/tasks/completions/test-task` removes record
- [ ] Unauthenticated requests return 401

### MongoDB Verification

```bash
# Connect with MongoDB Compass or CLI
mongosh "mongodb+srv://cluster.mongodb.net/gardening-dashboard" --username <user>

# Check collections
use gardening-dashboard
db.users.find()
db.taskCompletions.find()
```

---

## 📚 Learning Opportunities

This implementation covers:

**Scala Fundamentals:**
- Case classes and pattern matching
- Futures and async programming (`Future[T]`)
- Type-safe JSON serialization with Play JSON
- Dependency injection with Guice

**Backend Architecture:**
- **Repository Pattern** - data access abstraction
- **Service Layer** - business logic separation
- **RESTful API design** - resource-based endpoints
- **Session management** - cookies vs tokens

**MongoDB:**
- Document-based data modeling
- Async CRUD operations with Scala driver
- Upsert patterns (`replaceOne` with `upsert = true`)

**OAuth 2.0:**
- Authorization code flow
- Token exchange with Google APIs
- Session cookies for authentication

---

## 🔮 Future Phases

### Phase 2: Frontend Integration

- Create API service layer in Vue frontend (`src/services/api.ts`)
- Update `HomeTab.vue` to call task completion API
- Add login/logout UI component
- Handle localStorage → API migration for existing users

### Phase 3: Refactor to Cats Effect (Learning Exercise)

**Goal:** Refactor the backend from `Future` to Cats Effect `IO` to align with colleagues' codebases and learn functional effect systems.

**Why after Phase 1:**
- You'll understand the "why" after seeing Future-based approach work
- Compare both patterns side-by-side
- Builds on fundamentals (async, flatMap, composition) you've already learned

**Changes needed:**
- Add dependencies: `mongo4cats`, `cats-effect`
- Replace MongoDB Scala driver with `mongo4cats` wrapper
- Convert repositories to return `IO[T]` instead of `Future[T]`
- Add `.unsafeToFuture()` in controllers to bridge IO → Future
- Use `Resource[IO, MongoClient]` for automatic cleanup

**Learning outcomes:**
- `IO` monad vs `Future`
- Resource management with `Resource`
- Effect composition patterns
- Understanding Typelevel ecosystem (FS2, http4s)

**Estimated effort:** 1-2 days (mostly mechanical refactoring)

**Resources:**
- [mongo4cats library](https://github.com/Kirill5k/mongo4cats)
- [Play + Cats Effect integration](https://andres.jaimes.net/scala/cats-effect-io-play-framework/)

---

### Phase 4: Static Data Migration
- Move `gardenData.json`, `gardenPlanData.json`, etc. to MongoDB
- Create CRUD APIs for garden data
- Update frontend to fetch from API instead of JSON files

### Phase 5: Advanced Features
- Multi-garden support
- Sharing/collaboration
- Weather API integration
- Push notifications
