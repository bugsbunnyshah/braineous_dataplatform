cp ./pom.xml.clean ./pom.xml
mvn clean -U

cp ./pom.xml.package ./pom.xml

mvn package
java -jar target/braineous-tutorials-1.0.0-jar-with-dependencies.jar