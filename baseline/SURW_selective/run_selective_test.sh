#!/bin/bash
# SURW Selective Scheduling Test Script
# Usage: ./run_selective_test.sh [TestClass] [config_file]

set -e

JAVA_HOME=/home/bupt/jdk8
JAVA=$JAVA_HOME/bin/java

SURW_HOME=/mnt/f/vcf/baseline/SURW_selective
cd $SURW_HOME

TEST_CLASS=${1:-edu.tamu.aser.rff.RFFRaceTest}
CONFIG_FILE=${2:-selective_config.txt}

echo "=== SURW Selective Scheduling Test ==="
echo "Java: $($JAVA -version 2>&1 | head -1)"
echo "Test: $TEST_CLASS"
echo "Config: $CONFIG_FILE"
echo ""

# Check if config file exists
if [ ! -f "$CONFIG_FILE" ]; then
    echo "WARNING: Config file not found: $CONFIG_FILE"
    echo "Selective mode disabled, all events will be scheduled"
    echo ""
fi

timeout 30 $JAVA -ea -Xshare:off \
  -javaagent:libs/rff-agent-stub.jar \
  -cp ".:./build/*:./libs/asm.jar:./mcr-scheduler/target/test-classes:./mcr-scheduler/target/classes" \
  -Dmcr.properties=/rff.properties \
  -Dselective.config=$CONFIG_FILE \
  $TEST_CLASS

EXIT_CODE=$?

echo ""
echo "=== Test Completed (Exit: $EXIT_CODE) ==="
exit $EXIT_CODE
