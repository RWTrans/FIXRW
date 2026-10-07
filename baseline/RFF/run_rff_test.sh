#!/bin/bash
# RFF Test Runner Script
# Runs a test class with RFF strategy

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <test_class> [options]"
    echo "Example: $0 edu.tamu.aser.rff.SimpleRaceTest"
    exit 1
fi

TEST_CLASS=$1
shift

SURW_HOME="/mnt/f/vcf/baseline/SURW"
AGENT_JAR="${SURW_HOME}/libs/rff-agent.jar"
SCHEDULER_JAR="${SURW_HOME}/mcr-scheduler/target/classes"
INSTRUMENTOR_JAR="${SURW_HOME}/mcr-instrumentor/target/classes"
ASM_JAR="${SURW_HOME}/libs/asm.jar"
JUNIT_JAR="${SURW_HOME}/libs/junit-4.8.2.jar"

# Strategy class
RFF_STRATEGY="edu.tamu.aser.rff.RFFStrategy"

# Run with RFF strategy
java -ea \
    -javaagent:"${AGENT_JAR}" \
    -cp "${SCHEDULER_JAR}:${INSTRUMENTOR_JAR}:${ASM_JAR}:${JUNIT_JAR}:." \
    -Dmcr.exploration.scheduling.strategy="${RFF_STRATEGY}" \
    -Dmcr.exploration.timeout=300000 \
    -Ddebug=false \
    org.junit.runner.JUnitCore "${TEST_CLASS}" "$@"
