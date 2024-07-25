./build.sh

#cd cli
#./build.sh
#cd ..

./package_sdk_1.0.0-cr3.sh

rm -rf releases

mkdir releases/
mkdir releases/braineous-1.0.0-cr3

cd releases/braineous-1.0.0-cr3
mkdir bin
mkdir client-sdk
mkdir pipeline_monitor
mkdir tutorials


cd tutorials
mkdir get-started
mkdir datalake
mkdir data-transformation
mkdir create-connector
mkdir clickhouse-connector
mkdir elastic-connector
mkdir snowflake-connector

pwd

cd ..
pwd
cd ..
pwd
cd ..
pwd

#braineous
cp -r dataplatform-1.0.0-cr3-runner.jar releases/braineous-1.0.0-cr3/bin
cp -r start_braineous.sh releases/braineous-1.0.0-cr3/bin
cp -r conf releases/braineous-1.0.0-cr3/bin
cp -r test_installation/* releases/braineous-1.0.0-cr3/bin

#service dependencies
cp -r localhost-services/zookeeper/* releases/braineous-1.0.0-cr3/bin
cp -r localhost-services/kafka/* releases/braineous-1.0.0-cr3/bin
cp -r localhost-services/hive/* releases/braineous-1.0.0-cr3/bin
cp -r localhost-services/flink/* releases/braineous-1.0.0-cr3/bin



#client-sdk
cp -r braineous-dataingestion-sdk-1.0.0-cr3.jar releases/braineous-1.0.0-cr3/client-sdk

#pipeline_monitor
cp -r pipeline_monitor/pipemon releases/braineous-1.0.0-cr3/pipeline_monitor
cp -r pipeline_monitor/pipemon.sh releases/braineous-1.0.0-cr3/bin

#tutorials
cp -r tutorials/get-started/* releases/braineous-1.0.0-cr3/tutorials/get-started
cp -r tutorials/create-connector/* releases/braineous-1.0.0-cr3/tutorials/create-connector
cp -r tutorials/datalake/* releases/braineous-1.0.0-cr3/tutorials/datalake
cp -r tutorials/data-transformation/* releases/braineous-1.0.0-cr3/tutorials/data-transformation
cp -r tutorials/clickhouse-connector/* releases/braineous-1.0.0-cr3/tutorials/clickhouse-connector
cp -r tutorials/elastic-connector/* releases/braineous-1.0.0-cr3/tutorials/elastic-connector
cp -r tutorials/snowflake-connector/* releases/braineous-1.0.0-cr3/tutorials/snowflake-connector

#cleanup
cd releases
zip -r braineous-1.0.0-cr3.zip braineous-1.0.0-cr3
rm -rf braineous-1.0.0-cr3
unzip braineous-1.0.0-cr3.zip
cd ..

pwd


