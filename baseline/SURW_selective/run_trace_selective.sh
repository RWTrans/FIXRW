#!/bin/bash
# Trace-Based Selective Scheduling Test Script
# Usage: ./run_trace_selective.sh [TestClass]
#
# This script runs the test TWICE:
#   Run 1: Collect trace
#   Run 2: Selective scheduling based on analyzed trace

set -e

JAVA_HOME=/home/bupt/jdk8
JAVA=$JAVA_HOME/bin/java

SURW_HOME=/mnt/f/vcf/baseline/SURW_selective
cd $SURW_HOME

TEST_CLASS=${1:-edu.tamu.aser.rff.RFFRaceTest}
CONFIG_FILE=${2:-selective_config.txt}

echo "=== Trace-Based Selective Scheduling Test ==="
echo "Test: $TEST_CLASS"
echo "Config: $CONFIG_FILE"
echo ""

# Clean previous trace files
rm -f trace_events.txt interested_variables.txt

echo "--- Phase 1: Trace Collection ---"
timeout 60 $JAVA -ea -Xshare:off \
  -javaagent:libs/rff-agent-stub.jar \
  -cp ".:./build/*:./libs/asm.jar:./mcr-scheduler/target/test-classes:./mcr-scheduler/target/classes" \
  -Dmcr.properties=/rff.properties \
  -Dselective.config=$CONFIG_FILE \
  $TEST_CLASS || true

echo ""
echo "--- Phase 2: Selective Scheduling ---"
timeout 60 $JAVA -ea -Xshare:off \
  -javaagent:libs/rff-agent-stub.jar \
  -cp ".:./build/*:./libs/asm.jar:./mcr-scheduler/target/test-classes:./mcr-scheduler/target/classes" \
  -Dmcr.properties=/rff.properties \
  -Dselective.config=$CONFIG_FILE \
  $TEST_CLASS

EXIT_CODE=$?

echo ""
echo "=== Test Completed (Exit: $EXIT_CODE) ==="
exit $EXIT_CODE
