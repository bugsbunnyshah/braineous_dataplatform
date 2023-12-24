cp ./pom.xml.clean ./pom.xml
mvn clean -U

cp ./pom.xml.package ./pom.xml
mvn package

cp ../../../bin/dataplatform-1.0.0-cr2-runner.jar .

java -jar target/*-with-dependencies.jar