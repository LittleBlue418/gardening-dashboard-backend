# Gardening Dashboard: backend

Scala 3 + Play JSON API for the gardening dashboard. Data lives in JSON files in `temp-db/` for now (a real
database comes later).

## Run

From the repo root: `npm run start:all` (finds a JDK, starts the API on http://localhost:7710).

Tests: `sbt test` (needs `JAVA_HOME`, e.g. `export JAVA_HOME=/opt/homebrew/opt/openjdk@25`).

## Structure

Each folder has one responsibility:

```
app/
├── controllers/    # HTTP layer: the /api routes (conf/routes)
├── services/       # Business logic (e.g. MyPlantsService: selected plants ⊆ saved plants)
├── repositories/   # Data access (JSON files in temp-db/)
└── models/         # Domain models: the source of truth for all data types
```

`image-db/` holds plant photos from a private, watermarked book. It is gitignored: never commit or share it.
