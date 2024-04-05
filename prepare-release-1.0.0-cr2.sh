./build.sh

cd cli
./build.sh

cd ..

./package_sdk_1.0.0-cr2.sh

rm -rf releases/braineous-1.0.0-cr2
rm -rf releases/braineous-1.0.0-cr2.zip

mkdir releases/braineous-1.0.0-cr2
cd releases/braineous-1.0.0-cr2
mkdir bin
mkdir tutorials
mkdir client-sdk
mkdir pipeline_monitor

cd tutorials
mkdir get-started
mkdir create-connector
mkdir datalake
mkdir data-transformation


cd ..
cd ..
cd ..

pwd



cp -r dataplatform-1.0.0-cr2-runner.jar releases/braineous-1.0.0-cr2/bin
cp -r dataplatform-1.0.0-cr2-runner.jar tutorials/create-connector/server/lib
cp -r start_braineous.sh releases/braineous-1.0.0-cr2/bin
cp -r braineous-dataingestion-sdk-1.0.0-cr2.jar releases/braineous-1.0.0-cr2/client-sdk
cp -r releases/dependency/* releases/braineous-1.0.0-cr2/bin

cp -r tutorials/get-started/* releases/braineous-1.0.0-cr2/tutorials/get-started
cp -r tutorials/create-connector/* releases/braineous-1.0.0-cr2/tutorials/create-connector
cp -r tutorials/datalake/* releases/braineous-1.0.0-cr2/tutorials/datalake
cp -r tutorials/data-transformation/* releases/braineous-1.0.0-cr2/tutorials/data-transformation

cp -r cli/pipemon releases/braineous-1.0.0-cr2/pipeline_monitor

cd releases
zip -r braineous-1.0.0-cr2.zip braineous-1.0.0-cr2
rm -rf braineous-1.0.0-cr2
unzip braineous-1.0.0-cr2.zip
cd ..

pwd


