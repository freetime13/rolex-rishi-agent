#!/bin/sh
APP_BASE_NAME=`basename "$0"`
DIRNAME=`dirname "$0"`
[ -z "$DIRNAME" ] && DIRNAME="."
exec "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" "$@"
