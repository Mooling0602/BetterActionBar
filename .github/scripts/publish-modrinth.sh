#!/usr/bin/env bash
# Publish a version to Modrinth.
# Expects:
#   VERSION          – mod version number (e.g. "1.1.0")
#   MODRINTH_TOKEN   – Modrinth API token
#   release-artifacts/ – directory containing built JAR files
#   release-notes.txt  – changelog text for this version (may be empty)
#   build.gradle       – used to dynamically resolve mc_target -> MC version mapping
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

# ---- Collect game versions & file args from artifacts ----

GAME_VERSIONS=()
FILE_PARTS_NAMES=()
FILE_FORM_ARGS=()
PRIMARY_FIELD=""

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
  field_name="file_${mc_target//./_}"

  GAME_VERSIONS+=("$game_version")
  FILE_PARTS_NAMES+=("$field_name")
  FILE_FORM_ARGS+=(-F "$field_name=@$jar")

  if [[ "$mc_target" == "26.2" ]]; then
    PRIMARY_FIELD="$field_name"
  fi
done

if [[ ${#FILE_FORM_ARGS[@]} -eq 0 ]]; then
  echo "Error: no mod JAR files found in release-artifacts/"
  exit 1
fi

if [[ -z "$PRIMARY_FIELD" ]]; then
  PRIMARY_FIELD="${FILE_PARTS_NAMES[0]}"
fi

# ---- Build JSON payload ----

GAME_VERSIONS_JSON="$(printf '%s\n' "${GAME_VERSIONS[@]}" | jq -R . | jq -s -c .)"
FILE_PARTS_JSON="$(printf '%s\n' "${FILE_PARTS_NAMES[@]}" | jq -R . | jq -s -c .)"

CHANGELOG_TEXT="$(cat release-notes.txt 2>/dev/null || true)"
CHANGELOG_JSON="$(printf '%s\n' "$CHANGELOG_TEXT" | jq -Rs .)"

DATA="$(jq -n \
  --arg project_id "$PROJECT_ID" \
  --arg version_number "$VERSION" \
  --arg name "BetterActionBar $VERSION" \
  --argjson changelog "$CHANGELOG_JSON" \
  --argjson game_versions "$GAME_VERSIONS_JSON" \
  --argjson file_parts "$FILE_PARTS_JSON" \
  --arg primary_file "$PRIMARY_FIELD" \
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
    game_versions: $game_versions,
    version_type: "release",
    loaders: ["fabric"],
    featured: false,
    status: "listed",
    requested_status: "listed",
    file_parts: $file_parts,
    primary_file: $primary_file
  }')"

echo "Publishing version $VERSION to Modrinth..."
echo "  Game versions: ${GAME_VERSIONS[*]}"
echo "  Primary file: $PRIMARY_FIELD"

MODRINTH_DATA_FILE="$(mktemp)"
printf '%s\n' "$DATA" > "$MODRINTH_DATA_FILE"

RESPONSE="$(curl -sS -w '\n%{http_code}' -X POST "https://api.modrinth.com/v2/version" \
  -H "Authorization: $MODRINTH_TOKEN" \
  -F "data=@$MODRINTH_DATA_FILE;type=application/json" \
  "${FILE_FORM_ARGS[@]}")"

rm -f "$MODRINTH_DATA_FILE"

HTTP_CODE="$(printf '%s\n' "$RESPONSE" | tail -1)"
BODY="$(printf '%s\n' "$RESPONSE" | sed '$d')"

echo "HTTP $HTTP_CODE"

if [[ "$HTTP_CODE" -lt 200 || "$HTTP_CODE" -ge 300 ]]; then
  echo "Error: Modrinth publish failed (HTTP $HTTP_CODE)"
  printf '%s\n' "$BODY" | jq -r '.description // .error // "Unknown error"' 2>/dev/null || printf '%s\n' "$BODY"
  exit 1
fi

MODRINTH_VERSION_ID="$(printf '%s\n' "$BODY" | jq -r '.id')"
echo "Successfully published to Modrinth! Version ID: $MODRINTH_VERSION_ID"
