#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
: "${SDK:?Définir ANDROID_SDK_ROOT avec le chemin du SDK Android}"
BT="$SDK/build-tools/35.0.0"
PLATFORM="$SDK/platforms/android-35/android.jar"
BUILD="$PWD/build"
rm -rf "$BUILD"
mkdir -p "$BUILD/res" "$BUILD/gen" "$BUILD/classes" "$BUILD/dex" "$BUILD/package" dist
"$BT/aapt2" compile --dir app/src/main/res -o "$BUILD/res/compiled.zip"
"$BT/aapt2" link -o "$BUILD/base.apk" -I "$PLATFORM" --manifest app/src/main/AndroidManifest.xml --java "$BUILD/gen" -A app/src/main/assets "$BUILD/res/compiled.zip"
find app/src/main/java "$BUILD/gen" -name '*.java' > "$BUILD/sources.txt"
javac -encoding UTF-8 -source 8 -target 8 -bootclasspath "$PLATFORM:$BT/core-lambda-stubs.jar" -d "$BUILD/classes" @"$BUILD/sources.txt"
jar cf "$BUILD/classes.jar" -C "$BUILD/classes" .
"$BT/d8" --release --min-api 26 --lib "$PLATFORM" --output "$BUILD/dex" "$BUILD/classes.jar"
cp "$BUILD/base.apk" "$BUILD/unsigned.apk"
(cd "$BUILD/dex" && zip -q "$BUILD/unsigned.apk" classes.dex)
"$BT/zipalign" -f -p 4 "$BUILD/unsigned.apk" "$BUILD/aligned.apk"
# The private signing key stays outside Git. Keep it to sign future updates.
KEY="${ETINCELLE_KEYSTORE:-$PWD/.signing/etincelle.jks}"
mkdir -p "$(dirname "$KEY")"
if [ ! -f "$KEY" ]; then
 keytool -genkeypair -keystore "$KEY" -storepass android -keypass android -alias etincelle -dname 'CN=Etincelle, OU=Personal Android App' -keyalg RSA -keysize 2048 -validity 10000 -noprompt
fi
"$BT/apksigner" sign --ks "$KEY" --ks-key-alias etincelle --ks-pass pass:android --key-pass pass:android --out dist/Etincelle-1.0.0.apk "$BUILD/aligned.apk"
"$BT/apksigner" verify --verbose dist/Etincelle-1.0.0.apk
"$BT/aapt2" dump badging dist/Etincelle-1.0.0.apk | head -8
sha256sum dist/Etincelle-1.0.0.apk > dist/SHA256SUMS.txt
