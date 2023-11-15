mvn clean package -Dquarkus.package.main-class=dataplatform -DskipTests
cp target/*-runner.jar .