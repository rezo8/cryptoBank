#!/bin/bash

echo "Waiting for Kafka to be ready..."
while ! nc -z broker-1 19092; do
  sleep 1
done
echo "Kafka is up!"

echo "Creating topic 'transactionEvents'..."
/opt/kafka/bin/kafka-topics.sh --create --topic transactionEvents \
  --bootstrap-server broker-1:19092 \
  --partitions 3 \
  --replication-factor 2 \
  --if-not-exists

echo "Topic 'transactionEvents' created!"
