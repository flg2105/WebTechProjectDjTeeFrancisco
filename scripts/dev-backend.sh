#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

load_dotenv() {
  local dotenv_path="$1"
  [[ -f "$dotenv_path" ]] || return 0

  while IFS= read -r line || [[ -n "$line" ]]; do
    [[ -n "$line" ]] || continue
    [[ "$line" == \#* ]] && continue

    local key="${line%%=*}"
    local value="${line#*=}"

    key="${key%"${key##*[![:space:]]}"}"
    key="${key#"${key%%[![:space:]]*}"}"
    [[ -n "$key" ]] || continue

    value="${value%"${value##*[![:space:]]}"}"
    value="${value#"${value%%[![:space:]]*}"}"

    if [[ ( "$value" == \"*\" && "$value" == *\" ) || ( "$value" == \'*\' && "$value" == *\' ) ]]; then
      value="${value:1:${#value}-2}"
    fi

    export "${key}=${value}"
  done <"$dotenv_path"
}

load_dotenv "${ROOT_DIR}/.env"

if [[ "${PP_ECHO_ENV:-}" == "1" ]]; then
  echo "SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-}"
  echo "SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL:-}"
  echo "SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME:-}"
  echo "SPRING_DATASOURCE_PASSWORD=(len=${#SPRING_DATASOURCE_PASSWORD})"
  exit 0
fi

cd "${ROOT_DIR}/backend"
exec ./mvnw spring-boot:run
