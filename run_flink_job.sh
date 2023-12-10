./stop_flink.sh && ./start_flink.sh
./build.sh
mvn clean test -Dtest=PipelineServiceTests#ingestArray