#!/bin/bash
export JAVA_HOME=/home/bupt/jdk8
export PATH=$JAVA_HOME/bin:$PATH
cd /mnt/f/vcf/baseline/SURW

$JAVA_HOME/bin/java -ea \
  -javaagent:libs/rff-agent-java8.jar \
  -cp "libs/rff-agent-java8.jar:build/mcr-scheduler-1.0-SNAPSHOT.jar:build/mcr-instrumentor-1.0-SNAPSHOT.jar:/home/bupt/.m2/repository/junit/junit/4.12/junit-4.12.jar:/home/bupt/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:mcr-scheduler/target/test-classes" \
  -Dmcr.exploration.scheduling.strategy=edu.tamu.aser.rff.RFFStrategy \
  org.junit.runner.JUnitCore edu.tamu.aser.rff.SimpleRaceTest 2>&1 | head -80