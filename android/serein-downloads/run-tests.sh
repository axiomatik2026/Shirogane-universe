#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p app/build/tests
java -m jdk.compiler/com.sun.tools.javac.Main -d app/build/tests app/src/main/java/sn/amado/downloadguard/DownloadPolicy.java app/src/main/java/sn/amado/downloadguard/RiskSession.java tests/PolicyTests.java
java -cp app/build/tests PolicyTests
