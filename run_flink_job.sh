./stop_flink.sh && ./start_flink.sh
./build.sh
mvn test -Dtest=PipelineServiceTests#ingestArray