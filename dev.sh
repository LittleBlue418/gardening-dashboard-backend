#!/usr/bin/env bash
# Start the whole dashboard locally: ./dev.sh   (Ctrl+C stops everything)
#
#   backend   http://localhost:7710   (port set in backend/build.sbt)
#   frontend  http://localhost:7711   (port set in frontend/vite.config.ts) — TODO, see below
set -euo pipefail
cd "$(dirname "$0")"

# sbt needs a JDK. The macOS `java` stub doesn't work, so fall back to Homebrew's openjdk@25.
if [[ -z "${JAVA_HOME:-}" ]]; then
  JAVA_HOME="$(brew --prefix openjdk@25 2>/dev/null || true)"
  export JAVA_HOME
fi
if [[ ! -x "${JAVA_HOME:-}/bin/java" ]]; then
  echo "No JDK found. Install one (brew install openjdk@25) or set JAVA_HOME." >&2
  exit 1
fi

# Fail early if a port is taken (something from work, or a dev server that's still running).
for port in 7710 7711; do
  if lsof -iTCP:"$port" -sTCP:LISTEN -n -P >/dev/null 2>&1; then
    echo "Port $port is already in use:" >&2
    lsof -iTCP:"$port" -sTCP:LISTEN -n -P >&2
    exit 1
  fi
done

# On Ctrl+C or exit, stop every process this script started.
trap 'trap - EXIT; kill 0' INT TERM EXIT

# Backend. Play's dev mode stops when its input closes, so give it an input that never closes.
(cd backend && tail -f /dev/null | sbt run) &

# TODO: frontend, once the Vue app has moved into frontend/:
# (cd frontend && npm run dev) &

echo "Backend:  http://localhost:7710  (first start compiles, give it a minute)"
echo "Ctrl+C stops everything."
wait
