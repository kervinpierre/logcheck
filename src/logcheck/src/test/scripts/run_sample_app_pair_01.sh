#!/bin/bash -xv

JAVA_CMD=java
TEMP_DIR="/cygdrive/c/windows/temp"
TARGET_DIR="/cygdrive/c/src/sludev.com/logcheck/logcheck-sample-app/target"

JAR_FILE="logcheck-sample-app-0.9.jar"

JAR_ARGS01="--output-file $TEMP_DIR/logcheck-sample-app-output-01.txt --delete-logs  --output-frequency 50ms --stop-after-count 1K"
JAR_ARGS02="--output-file $TEMP_DIR/logcheck-sample-app-output-02.txt --delete-logs  --output-frequency 50ms --stop-after-count 1K"

cd $TARGET_DIR

$JAVA_CMD -jar $JAR_FILE $JAR_ARGS01 &
$JAVA_CMD -jar $JAR_FILE $JAR_ARGS02 &

