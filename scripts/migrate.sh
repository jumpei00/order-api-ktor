#!/usr/bin/env bash
# Apply Flyway migrations to the local compose database.
set -euo pipefail

source "$(dirname "${BASH_SOURCE[0]}")/_env.sh"

docker compose up -d --wait db
./gradlew run --args="migrate"
