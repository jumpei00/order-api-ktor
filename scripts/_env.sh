# Shared helper: load .env and build host-side DB settings.
# Source this file; do not execute it directly.

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

if [ ! -f .env ]; then
  echo ".env not found. Copy .env.example to .env first." >&2
  exit 1
fi

set -a
# shellcheck disable=SC1091
source .env
set +a

# The app runs on the host here, so connect to the published port instead of
# the compose-internal host name `db`.
export DB_URL="jdbc:postgresql://localhost:${DB_PORT:-5432}/${DB_NAME}"
export JOOQ_DB_URL="$DB_URL"
