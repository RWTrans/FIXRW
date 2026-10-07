#!/bin/bash
set -e

JAVA_HOME=/home/bupt/jdk8
JAVA=$JAVA_HOME/bin/java

SURW_HOME=/mnt/f/vcf/baseline/SURW
cd $SURW_HOME

TEST_CLASS=${1:-edu.tamu.aser.rff.SimpleRaceTest}

echo "=== RFF Test with Java 8 ==="
echo "Java: $($JAVA -version 2>&1 | head -1)"
echo "Test: $TEST_CLASS"
echo ""

timeout 30 $JAVA -ea \
  -javaagent:libs/rff-agent-stub.jar \
  -cp ".:./build/*:./libs/asm.jar:./mcr-scheduler/target/test-classes" \
  -Dmcr.properties=/rff.properties \
  -Ddebug=false \
  $TEST_CLASS 2>&1 | head -100
