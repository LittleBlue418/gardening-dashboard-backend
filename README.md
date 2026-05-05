# Gardening Dashboard Backend

A Scala backend built with Play Framework and MongoDB to support the [Gardening Dashboard](../gardening-dashboard) Vue.js frontend.

## Tech Stack

- **Framework:** Play Framework (Scala)
- **Database:** MongoDB Atlas
- **Authentication:** Google OAuth 2.0
- **Build Tool:** SBT

## Project Status

🚧 **In Development** - Phase 1: Task Completion API

## Quick Start

```bash
# Start the backend server
sbt run

# Server runs on http://localhost:9000
```

## Implementation Plan

📋 See [IMPLEMENTATION_PLAN.md](./IMPLEMENTATION_PLAN.md) for the complete step-by-step guide.

**Phase 1 Scope:**
- Google OAuth authentication
- Task completion API (replaces localStorage)
- MongoDB integration
- CORS setup for frontend integration

## Project Structure

```
app/
├── controllers/      # API endpoints
├── models/          # Domain models (User, TaskCompletion)
├── repositories/    # MongoDB data access layer
└── services/        # Business logic (AuthService)
conf/
├── application.conf # Configuration
└── routes          # API routes
build.sbt           # Dependencies
test/               # Tests
```

## Related Projects

- **Frontend:** [../gardening-dashboard](../gardening-dashboard)
- **Data Schema:** See IMPLEMENTATION_PLAN.md for MongoDB collections

## Learning Goals

This project is designed to teach:
- Scala web development with Play Framework
- MongoDB with the Scala driver
- OAuth 2.0 authentication flow
- RESTful API design
- Full-stack integration

---

*See IMPLEMENTATION_PLAN.md for detailed setup instructions and code examples.*
