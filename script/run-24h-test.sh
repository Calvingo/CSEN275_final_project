#!/usr/bin/env bash
set -e
cd "$(dirname "$0")/.."
mvn -q exec:java -Dexec.mainClass=com.csen275.garden.app.HeadlessSimulationRunner
