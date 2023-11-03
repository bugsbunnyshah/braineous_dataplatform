mvn clean package -DskipTests && rm ./dataplatform-1.0.0-runner.jar && cp target/dataplatform-1.0.0-runner.jar . && mvn test -Dtest=EventProcessorTests#processEventWithPipeline



