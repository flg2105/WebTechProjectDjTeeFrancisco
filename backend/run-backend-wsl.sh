#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

GW="$(ip route | awk '/default/ {print $3; exit}')"
if [[ -z "${GW}" ]]; then
  echo "Could not determine WSL gateway IP (default route)." >&2
  exit 1
fi

export SPRING_DATASOURCE_URL="jdbc:mysql://${GW}:3306/project_pulse?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export SPRING_DATASOURCE_USERNAME="projectpulse"
export SPRING_DATASOURCE_PASSWORD="projectpulse"

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"

echo "Using MySQL host: ${GW}:3306"
echo "Using Spring profile(s): ${SPRING_PROFILES_ACTIVE}"
exec ./mvnw spring-boot:run
