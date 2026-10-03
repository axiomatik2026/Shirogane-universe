#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
SDK="${ANDROID_SDK_ROOT:?Définir ANDROID_SDK_ROOT}"
BT="${SHIROGANE_BUILD_TOOLS:-$SDK/build-tools/35.0.0}"
PLATFORM="${SHIROGANE_ANDROID_JAR:-$SDK/platforms/android-35/android.jar}"
BUILD="$PWD/build"
mkdir -p "$BUILD/res" "$BUILD/gen" "$BUILD/classes" "$BUILD/dex" releases
"$BT/aapt2" compile --dir app/src/main/res -o "$BUILD/res/compiled.zip"
"$BT/aapt2" link -o "$BUILD/base.apk" -I "$PLATFORM" --manifest app/src/main/AndroidManifest.xml --java "$BUILD/gen" -A ../dist "$BUILD/res/compiled.zip"
find app/src/main/java "$BUILD/gen" -name '*.java' > "$BUILD/sources.txt"
if [ -n "${SHIROGANE_ECJ:-}" ];then
 java -jar "$SHIROGANE_ECJ" -8 -encoding UTF-8 -bootclasspath "$PLATFORM:$BT/core-lambda-stubs.jar" -d "$BUILD/classes" @"$BUILD/sources.txt"
else
 javac -encoding UTF-8 -source 8 -target 8 -bootclasspath "$PLATFORM:$BT/core-lambda-stubs.jar" -d "$BUILD/classes" @"$BUILD/sources.txt"
fi
python3 - "$BUILD" <<'PY'
import sys,pathlib,zipfile
p=pathlib.Path(sys.argv[1])
with zipfile.ZipFile(p/'classes.jar','w') as z:
 for f in (p/'classes').rglob('*.class'):z.write(f,f.relative_to(p/'classes'))
PY
"$BT/d8" --release --min-api 26 --lib "$PLATFORM" --output "$BUILD/dex" "$BUILD/classes.jar"
python3 - "$BUILD" <<'PY'
import sys,pathlib,zipfile,shutil
p=pathlib.Path(sys.argv[1]);shutil.copy(p/'base.apk',p/'unsigned.apk')
with zipfile.ZipFile(p/'unsigned.apk','a') as z:z.write(p/'dex/classes.dex','classes.dex')
PY
"$BT/zipalign" -f -p 4 "$BUILD/unsigned.apk" "$BUILD/aligned.apk"
KEY="${SHIROGANE_KEYSTORE:-$PWD/.signing/shirogane.jks}"
mkdir -p "$(dirname "$KEY")"
if [ ! -f "$KEY" ];then
 keytool -genkeypair -keystore "$KEY" -storepass android -keypass android -alias shirogane -dname 'CN=Shirogane Wiki, OU=Axiomatik' -keyalg RSA -keysize 2048 -validity 10000 -noprompt
fi
"$BT/apksigner" sign --ks "$KEY" --ks-key-alias shirogane --ks-pass pass:android --key-pass pass:android --out releases/Shirogane-Wiki-1.0.0.apk "$BUILD/aligned.apk"
"$BT/apksigner" verify --verbose releases/Shirogane-Wiki-1.0.0.apk
"$BT/zipalign" -c -p 4 releases/Shirogane-Wiki-1.0.0.apk
"$BT/aapt2" dump badging releases/Shirogane-Wiki-1.0.0.apk | head -8
sha256sum releases/Shirogane-Wiki-1.0.0.apk > releases/SHA256SUMS.txt
