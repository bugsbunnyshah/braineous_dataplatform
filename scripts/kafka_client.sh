/Users/babyboy/mumma/braineous/infrastructure/kafka_2.13-3.5.0/bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server localhost:9092

/Users/babyboy/mumma/braineous/infrastructure/kafka_2.13-3.5.0/bin/kafka-topics.sh --describe --topic quickstart-events --bootstrap-server localhost:9092

/Users/babyboy/mumma/braineous/infrastructure/kafka_2.13-3.5.0/bin/kafka-console-producer.sh --topic quickstart-events --bootstrap-server localhost:9092
