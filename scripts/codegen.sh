#!/usr/bin/env bash
# Regenerate jOOQ code (src/main/generated) from the migrated local database.
set -euo pipefail

source "$(dirname "${BASH_SOURCE[0]}")/_env.sh"

docker compose up -d --wait db
./gradlew jooqCodegen
