#!/bin/bash
set -e

JAVA_HOME=/home/bupt/jdk8
JAVA=$JAVA_HOME/bin/java

SURW_HOME=/mnt/f/vcf/baseline/SURW
cd $SURW_HOME

echo "=== Test Properties Loading ==="
timeout 10 $JAVA -cp ".:./build/*:./libs/asm.jar:./mcr-scheduler/target/test-classes" \
  -Dmcr.properties=/rff.properties \
  edu.tamu.aser.rff.TestProperties 2>&1
