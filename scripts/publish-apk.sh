#!/usr/bin/env bash
# Builds release APKs (Factory & Textiles) and publishes them as the newest GitHub Release.
# Release notes are extracted from CHANGELOG.md under "## Unreleased" or "## <version>".
#
# Usage:
#   ./scripts/publish-apk.sh            # build, then publish
#   ./scripts/publish-apk.sh --no-build # publish existing APKs in build folder
set -euo pipefail
cd "$(dirname "$0")/.."

GH="$(command -v gh || echo "$HOME/.local/bin/gh")"

# Read version from build.gradle.kts
version="$(sed -n 's/.*versionName = "\(.*\)".*/\1/p' kmp/composeApp/build.gradle.kts)"
tag="v$version"

# Extract notes from CHANGELOG.md
section() { awk -v h="$1" '/^## /{ if (on) exit; on = (substr($0, 4) == h); next } on' CHANGELOG.md; }
from="$version"
notes="$(section "$version")"
if ! grep -q '^[-*] ' <<<"$notes"; then
  from="Unreleased"
  notes="$(section Unreleased)"
  grep -q '^[-*] ' <<<"$notes" || { echo "CHANGELOG.md has no notes for $version: add them under '## Unreleased'" >&2; exit 1; }
fi

if [[ "${1:-}" != "--no-build" ]]; then
  echo "Building release APKs for Factory and Textiles..."
  cd kmp
  ./gradlew assembleFactoryRelease assembleTextilesRelease --console=plain
  cd ..
fi

tmp_dir="$(mktemp -d)"
factory_apk="$tmp_dir/Udukkai-Factory.apk"
textiles_apk="$tmp_dir/Udukkai-Textiles.apk"

cp kmp/composeApp/build/outputs/apk/factory/release/composeApp-factory-release.apk "$factory_apk"
cp kmp/composeApp/build/outputs/apk/textiles/release/composeApp-textiles-release.apk "$textiles_apk"

body="$tmp_dir/notes.md"
printf '%s\n\n---\n\nInstall Udukkai-Factory.apk or Udukkai-Textiles.apk. Installed copies update automatically from the in-app updater.\n' \
  "$(sed -e '/./,$!d' <<<"$notes")" >"$body"

if "$GH" release view "$tag" >/dev/null 2>&1; then
  echo "Updating existing release $tag..."
  "$GH" release upload "$tag" "$factory_apk" "$textiles_apk" --clobber
  "$GH" release edit "$tag" --latest --notes-file "$body"
else
  echo "Creating new release $tag..."
  "$GH" release create "$tag" "$factory_apk" "$textiles_apk" --title "Udukkai $version" --latest --notes-file "$body"
fi

if [[ "$from" == "Unreleased" ]]; then
  V="$version" perl -0pi -e 's/^## Unreleased\n/## Unreleased\n\n## $ENV{V}\n/m' CHANGELOG.md
  echo "CHANGELOG.md: Unreleased is now $version (commit it)"
fi

repo="$("$GH" repo view --json nameWithOwner -q .nameWithOwner)"
echo "Published $tag: https://github.com/$repo/releases/latest"
