#!/usr/bin/env bash
# Publish each built JAR as a separate Modrinth version, each with its own
# Minecraft game version.  Modrinth's API does not support per-file game
# versions inside a single version, so we create one version per MC target.
#
# Expects:
#   VERSION            – base mod version (e.g. "1.1.0")
#   MODRINTH_TOKEN     – Modrinth API token
#   release-artifacts/ – directory containing built JAR files
#   release-notes.txt  – changelog text (may be empty)
#   build.gradle       – mc_target -> MC version mapping
set -euo pipefail

if [[ -z "${VERSION:-}" ]]; then
  echo "Error: VERSION is not set"
  exit 1
fi
if [[ -z "${MODRINTH_TOKEN:-}" ]]; then
  echo "Error: MODRINTH_TOKEN is not set"
  exit 1
fi

# ---- Parse mc_target -> minecraft version from build.gradle ----

TARGET_CONFIGS="$(grep -E '^[[:space:]]*"[^"]+":[[:space:]]*\[minecraft:[[:space:]]*"[^"]+"' build.gradle | \
                  sed 's/^[[:space:]]*"\([^"]*\)":[[:space:]]*\[minecraft:[[:space:]]*"\([^"]*\)".*/\1=\2/' || true)"

declare -A MC_TARGET_TO_GAME_VERSION
while IFS='=' read -r target mc_ver; do
  [[ -n "$target" ]] && MC_TARGET_TO_GAME_VERSION["$target"]="$mc_ver"
done <<< "$TARGET_CONFIGS"

if [[ ${#MC_TARGET_TO_GAME_VERSION[@]} -eq 0 ]]; then
  echo "Error: failed to parse targetConfigs from build.gradle"
  exit 1
fi
echo "Parsed MC target mapping from build.gradle:"
for k in "${!MC_TARGET_TO_GAME_VERSION[@]}"; do
  echo "  $k -> ${MC_TARGET_TO_GAME_VERSION[$k]}"
done

# ---- Resolve Modrinth project ID ----

PROJECT=$(curl -sS -H "Authorization: $MODRINTH_TOKEN" \
  "https://api.modrinth.com/v2/project/betteractionbar")
PROJECT_ID=$(echo "$PROJECT" | jq -r '.id')
if [[ -z "$PROJECT_ID" || "$PROJECT_ID" == "null" ]]; then
  echo "Error: unable to resolve Modrinth project for slug 'betteractionbar'"
  exit 1
fi
echo "Resolved Modrinth project: $PROJECT_ID"

# ---- Read changelog once ----

CHANGELOG_TEXT="$(cat release-notes.txt 2>/dev/null || true)"
CHANGELOG_JSON="$(printf '%s\n' "$CHANGELOG_TEXT" | jq -Rs .)"

# ---- Publish one version per JAR ----

FAILURES=0
PUBLISHED=0

for jar in release-artifacts/*.jar; do
  [[ -f "$jar" ]] || continue
  filename="$(basename "$jar")"

  # Skip sources and dev JARs
  case "$filename" in
    *-sources*|*-dev*) continue ;;
  esac

  mc_target="$(printf '%s\n' "$filename" | sed -n 's/.*+mc\(.*\)\.jar/\1/p')"
  if [[ -z "$mc_target" ]]; then
    echo "Warning: could not extract MC target from $filename, skipping"
    continue
  fi

  game_version="${MC_TARGET_TO_GAME_VERSION[$mc_target]:-$mc_target}"
  full_version="${VERSION}+mc${mc_target}"

  echo ""
  echo "--- Publishing $full_version for MC $game_version ---"

  DATA="$(jq -n \
    --arg project_id "$PROJECT_ID" \
    --arg version_number "$full_version" \
    --arg name "BetterActionBar $VERSION" \
    --argjson changelog "$CHANGELOG_JSON" \
    --arg game_version "$game_version" \
    '{
      project_id: $project_id,
      name: $name,
      version_number: $version_number,
      changelog: $changelog,
      dependencies: [{
        project_id: "P7dR8mSH",
        version_id: null,
        file_name: null,
        dependency_type: "optional"
      }],
      game_versions: [$game_version],
      version_type: "release",
      loaders: ["fabric"],
      featured: false,
      status: "listed",
      requested_status: "listed",
      file_parts: ["file"],
      primary_file: "file"
    }')"

  MODRINTH_DATA_FILE="$(mktemp)"
  printf '%s\n' "$DATA" > "$MODRINTH_DATA_FILE"

  RESPONSE="$(curl -sS -w '\n%{http_code}' -X POST "https://api.modrinth.com/v2/version" \
    -H "Authorization: $MODRINTH_TOKEN" \
    -F "data=@$MODRINTH_DATA_FILE;type=application/json" \
    -F "file=@$jar")"

  rm -f "$MODRINTH_DATA_FILE"

  HTTP_CODE="$(printf '%s\n' "$RESPONSE" | tail -1)"
  BODY="$(printf '%s\n' "$RESPONSE" | sed '$d')"

  if [[ "$HTTP_CODE" -lt 200 || "$HTTP_CODE" -ge 300 ]]; then
    echo "FAILED (HTTP $HTTP_CODE)"
    printf '%s\n' "$BODY" | jq -r '.description // .error // "Unknown error"' 2>/dev/null || printf '%s\n' "$BODY"
    FAILURES=$((FAILURES + 1))
  else
    MODRINTH_VERSION_ID="$(printf '%s\n' "$BODY" | jq -r '.id')"
    echo "OK – Modrinth version ID: $MODRINTH_VERSION_ID"
    PUBLISHED=$((PUBLISHED + 1))
  fi
done

echo ""
echo "=== Modrinth publish summary ==="
echo "  Published: $PUBLISHED"
echo "  Failed:    $FAILURES"

if [[ $FAILURES -gt 0 ]]; then
  exit 1
fi
