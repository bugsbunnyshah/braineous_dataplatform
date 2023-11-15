mvn clean package -Dquarkus.package.main-class=dataplatform -DskipTests

cp target/*-runner.jar .
cp target/*-runner.jar ./braineous-dataingestion-sdk-1.0.0-cr1.jar