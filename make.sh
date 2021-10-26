#!/bin/bash
mvn clean compile assembly:single package
mkdir -p worker-jars
mv target/*dependencies.jar worker-jars/
