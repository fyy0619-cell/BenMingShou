#!/usr/bin/env bash
# Builds the signed release and assembles release/out/: the APK plus a filled-in download page.
# Usage: bash release/package.sh
set -euo pipefail
cd "$(dirname "$0")/.."
source /d/DevTools/env.sh

./gradlew testDebugUnitTest assembleRelease --console=plain -q

version=$(sed -nE 's/.*versionName = "([^"]+)".*/\1/p' app/build.gradle.kts)
apk="BenMingShou-${version}.apk"
out=release/out
rm -rf "$out" && mkdir -p "$out"
cp app/build/outputs/apk/release/app-release.apk "$out/$apk"

if aapt2.exe dump permissions "$out/$apk" | grep -q "android.permission.INTERNET"; then
  echo "refusing to package: INTERNET permission found" >&2
  exit 1
fi

size=$(awk -v b="$(stat -c %s "$out/$apk")" 'BEGIN { printf "%.1f MB", b / 1048576 }')
sha=$(sha256sum "$out/$apk" | cut -d' ' -f1)
sed -e "s/{{APK}}/$apk/g" -e "s/{{VERSION}}/$version/g" -e "s/{{SIZE}}/$size/g" -e "s/{{SHA256}}/$sha/g" \
  release/page.html > "$out/index.html"

echo "release/out ready: $apk ($size)"
echo "sha256 $sha"
