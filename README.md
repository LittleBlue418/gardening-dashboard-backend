# Gardening Dashboard

A gardening dashboard for a cold-climate (Stockholm) kitchen garden.

| Folder | What it is |
|---|---|
| `backend/` | Scala + Play JSON API (port 7710). See `backend/README.md` |
| `frontend/` | Vue 3 + TypeScript + Vite app (port 7711, moving in next) |

## Run locally

```bash
npm run start:all   # starts everything; Ctrl+C stops it
```

Backend on http://localhost:7710. Frontend on http://localhost:7711 (TODO in `dev.sh` until the Vue app moves in).
The ports are deliberately not the defaults (9000, 5173) so they never clash with work services.
