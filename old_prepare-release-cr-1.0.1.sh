./build.sh

rm -rf release-cr-1.0.1

mkdir release-cr-1.0.1
cd release-cr-1.0.1
mkdir braineous-1.0.1-cr1
cd braineous-1.0.1-cr1
mkdir bin
mkdir tutorials
cd tutorials
mkdir get-started
mkdir create-connector


cd ..
cd ..
cd ..

cp -r releases/braineous-1.0.1-cr1/bin/* release-cr-1.0.1/braineous-1.0.1-cr1/bin
cp dataplatform-1.0.1-cr1-runner.jar release-cr-1.0.1/braineous-1.0.1-cr1/bin/dataplatform-1.0.1-runner.jar
cp -r tutorials/get-started/* release-cr-1.0.1/braineous-1.0.1-cr1/tutorials/get-started
cp -r tutorials/get-started/* release-cr-1.0.1/braineous-1.0.1-cr1/tutorials/create-connector

cd release-cr-1.0.1
zip -r braineous-1.0.1-cr1.zip braineous-1.0.1-cr1
cd ..

pwd


