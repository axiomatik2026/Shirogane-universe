#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p app/build/core-tests
java -m jdk.compiler/com.sun.tools.javac.Main -d app/build/core-tests app/src/main/java/sn/amado/serein/Policy.java app/src/main/java/sn/amado/serein/DnsPacket.java app/src/main/java/sn/amado/serein/DnsHttps.java app/src/main/java/sn/amado/serein/SearchPolicy.java tests/CoreTests.java tests/HttpsTests.java tests/SearchTests.java app/src/main/java/sn/amado/serein/LocalGuardState.java tests/BackgroundTests.java tests/NoNetworkRunner.java
java -cp app/build/core-tests CoreTests
java -cp app/build/core-tests HttpsTests
java -cp app/build/core-tests SearchTests

java -Djava.security.manager=allow -cp app/build/core-tests NoNetworkRunner
