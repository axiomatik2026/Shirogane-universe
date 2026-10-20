#!/usr/bin/env bash
# Build a signed Android App Bundle candidate with Google's bundletool.
set -euo pipefail
cd "$(dirname "$0")"
: "${JAVA_HOME:?Set JAVA_HOME to JDK 17}"
: "${ANDROID_HOME:?Set ANDROID_HOME to the Android SDK}"
: "${BUNDLETOOL_JAR:?Set BUNDLETOOL_JAR to the official bundletool-all jar}"
: "${SEREIN_KEYSTORE:?Provide the existing private signing key; never commit it}"
VERSION_NAME="1.3.0-rc1"
VERSION_CODE="5"
BT="$ANDROID_HOME/build-tools/36.0.0"
PLATFORM="$ANDROID_HOME/platforms/android-36/android.jar"
OUT="$PWD/app/build/manual"
BUNDLE_OUT="$PWD/app/build/bundle"
# Compile the exact same sources/assets as the test APK.
bash build-apk.sh
mkdir -p "$BUNDLE_OUT"
"$BT/aapt2" link --proto-format -o "$BUNDLE_OUT/proto.apk" -I "$PLATFORM" --manifest "$OUT/AndroidManifest.xml" \
 --min-sdk-version 26 --target-sdk-version 36 --version-code "$VERSION_CODE" --version-name "$VERSION_NAME" \
 -A app/src/main/assets "$OUT/resources.zip"
python3 - "$BUNDLE_OUT/proto.apk" "$OUT/dex" "$BUNDLE_OUT/base.zip" <<'PY'
from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import sys
proto,dex,target=sys.argv[1:]
with ZipFile(proto) as source,ZipFile(target,'w',ZIP_DEFLATED) as out:
 for item in source.infolist():
  if item.is_dir():continue
  name='manifest/AndroidManifest.xml' if item.filename=='AndroidManifest.xml' else item.filename
  out.writestr(name,source.read(item.filename))
 for p in sorted(Path(dex).glob('classes*.dex')):out.write(p,'dex/'+p.name)
PY
"$JAVA_HOME/bin/java" -jar "$BUNDLETOOL_JAR" build-bundle --modules="$BUNDLE_OUT/base.zip" --output="$BUNDLE_OUT/Serein-$VERSION_NAME.aab" --overwrite
# Existing local release certificate; Google Play App Signing enrollment must be
# decided in Play Console before production (upload key and app-signing key differ).
"$JAVA_HOME/bin/java" -m jdk.jartool/sun.security.tools.jarsigner.Main \
 -keystore "$SEREIN_KEYSTORE" -storepass android -keypass android \
 -sigalg SHA256withRSA -digestalg SHA-256 "$BUNDLE_OUT/Serein-$VERSION_NAME.aab" serein
"$JAVA_HOME/bin/java" -jar "$BUNDLETOOL_JAR" validate --bundle="$BUNDLE_OUT/Serein-$VERSION_NAME.aab"
"$JAVA_HOME/bin/java" -m jdk.jartool/sun.security.tools.jarsigner.Main -verify "$BUNDLE_OUT/Serein-$VERSION_NAME.aab"
echo "$BUNDLE_OUT/Serein-$VERSION_NAME.aab"
