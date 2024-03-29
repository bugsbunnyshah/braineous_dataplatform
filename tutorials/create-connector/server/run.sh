cp ./pom.xml.clean ./pom.xml
mvn clean -U

cp ./pom.xml.package ./pom.xml
mvn package

java -jar target/*-with-dependencies.jar