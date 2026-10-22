#!/usr/bin/env bash
# Reproducible platform-tools build; no Maven dependencies or Gradle required.
set -euo pipefail
cd "$(dirname "$0")"
: "${ANDROID_HOME:?Set ANDROID_HOME to your Android SDK directory}"
: "${JAVA_HOME:?Set JAVA_HOME to JDK 17 or newer}"
SDK="$ANDROID_HOME"
BT="$SDK/build-tools/35.0.0"
PLATFORM="$SDK/platforms/android-35/android.jar"
OUT="$PWD/app/build/manual"
mkdir -p "$OUT/classes" "$OUT/generated" "$OUT/dex"
"$BT/aapt2" compile --dir app/src/main/res -o "$OUT/resources.zip"
sed 's/<manifest /<manifest package="sn.amado.serein" /' app/src/main/AndroidManifest.xml > "$OUT/AndroidManifest.xml"
"$BT/aapt2" link -o "$OUT/base.apk" -I "$PLATFORM" --manifest "$OUT/AndroidManifest.xml" --java "$OUT/generated" --min-sdk-version 26 --target-sdk-version 35 --version-code 6 --version-name 1.3.0 "$OUT/resources.zip"
find app/src/main/java "$OUT/generated" -name '*.java' > "$OUT/sources.list"
"$JAVA_HOME/bin/java" -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -source 8 -target 8 -bootclasspath "$PLATFORM:$BT/core-lambda-stubs.jar" -d "$OUT/classes" @"$OUT/sources.list"
"$JAVA_HOME/bin/java" -m jdk.jartool/sun.tools.jar.Main cf "$OUT/classes.jar" -C "$OUT/classes" .
"$BT/d8" --lib "$PLATFORM" --min-api 26 --output "$OUT/dex" "$OUT/classes.jar"
cp "$OUT/base.apk" "$OUT/unaligned.apk"
(cd "$OUT/dex" && zip -q "$OUT/unaligned.apk" classes*.dex)
"$BT/zipalign" -f -p 4 "$OUT/unaligned.apk" "$OUT/aligned.apk"
# This local testing key is never committed. Keep it to install future updates.
KEY="${SEREIN_KEYSTORE:-$PWD/app/build/serein-local.jks}"
if [[ ! -f "$KEY" ]]; then
 "$JAVA_HOME/bin/keytool" -genkeypair -keystore "$KEY" -storepass android -keypass android -alias serein -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=Serein Local Build' >/dev/null 2>&1
fi
"$BT/apksigner" sign --ks "$KEY" --ks-key-alias serein --ks-pass pass:android --key-pass pass:android --out "$OUT/Serein-1.3.0.apk" "$OUT/aligned.apk"
"$BT/apksigner" verify --verbose "$OUT/Serein-1.3.0.apk"
echo "$OUT/Serein-1.3.0.apk"
