mvn clean package -DskipTests && rm ./dataplatform-1.0.0-runner.jar && cp target/dataplatform-1.0.0-runner.jar .

java -jar target/dataplatform-1.0.0-runner.jar

