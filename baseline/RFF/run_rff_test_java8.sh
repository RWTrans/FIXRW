#!/bin/bash
set -e

# RFF with Java 8 - uses original ASM from libs/asm.jar (embedded in agent)
JAVA_HOME=/home/bupt/jdk8
JAVA=$JAVA_HOME/bin/java

# Paths
SURW_HOME=/mnt/f/vcf/baseline/SURW
AGENT_JAR=$SURW_HOME/libs/rff-agent-java8.jar
SCHEDULER_JAR=$SURW_HOME/build/mcr-scheduler-1.0-SNAPSHOT.jar
INSTRUMENTOR_JAR=$SURW_HOME/build/mcr-instrumentor-1.0-SNAPSHOT.jar
JUNIT_JAR=/home/bupt/.m2/repository/junit/junit/4.12/junit-4.12.jar
HAMCREST_JAR=/home/bupt/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar

# Test class
TEST_CLASS=${1:-edu.tamu.aser.rff.SimpleRaceTest}

echo "=== RFF Test with Java 8 ==="
echo "Java: $($JAVA -version 2>&1 | head -1)"
echo "Agent: $AGENT_JAR"
echo "Test: $TEST_CLASS"
echo ""

# Run with javaagent; agent jar must be FIRST in classpath so its ASM wins
# scheduler jar must be in classpath so instrumented classes can call Scheduler methods
$JAVA -ea \
  -javaagent:$AGENT_JAR \
  -cp "$AGENT_JAR:$SCHEDULER_JAR:$INSTRUMENTOR_JAR:$JUNIT_JAR:$HAMCREST_JAR:$SURW_HOME/mcr-scheduler/target/test-classes" \
  -Dmcr.exploration.scheduling.strategy=edu.tamu.aser.rff.RFFStrategy \
  org.junit.runner.JUnitCore $TEST_CLASS
