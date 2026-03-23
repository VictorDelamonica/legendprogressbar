#!/usr/bin/env sh
# Minimal gradlew wrapper script. If gradle-wrapper.jar is present, this will use the wrapper; otherwise it delegates to a system Gradle.
DIR="$(cd "$(dirname "$0")" && pwd)"
if [ -x "$DIR/gradlew" ] && [ "$0" != "$DIR/gradlew" ]; then
  exec "$DIR/gradlew" "$@"
fi
if [ -f "$DIR/gradle/wrapper/gradle-wrapper.jar" ]; then
  java -jar "$DIR/gradle/wrapper/gradle-wrapper.jar" "$@"
else
  exec gradle "$@"
fi
