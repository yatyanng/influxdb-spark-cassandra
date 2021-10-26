#!/bin/bash
rm -f logs/*
java -Dhadoop.home.dir=/tmp -Dserver.port=8084 -Dhibernate.dialect=org.hibernate.dialect.PostgreSQLDialect -jar target/influxdb-spark-cassandra-1.0.2.jar
