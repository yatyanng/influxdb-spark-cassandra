#!/bin/bash
cd /opt/zookeeper-3.4.10
sudo bin/zkServer.sh start
cd /opt/kafka_2.12-1.0.0/
sudo ./run.sh 
sleep 1
cd /opt/spark
sudo sbin/start-master.sh
sudo sbin/start-slave.sh spark://Yats-Mac-mini.local:7077
cd /opt/cassandra
sudo -u cassandra bin/cassandra

