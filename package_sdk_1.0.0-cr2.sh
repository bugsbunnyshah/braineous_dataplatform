rm -rf ./tmp
mkdir ./tmp

cd tmp
mkdir com
mkdir oauth
cd com
mkdir appgallabs
cd appgallabs
mkdir dataplatform
cd dataplatform
mkdir client
mkdir util
mkdir ingestion
cd ingestion
mkdir util
cd ..


cd client
mkdir sdk
cd sdk
mkdir api
mkdir infrastructure
mkdir network
mkdir service

cd ..
cd ..
cd ..
cd ..
cd ..
cd ..

pwd


cd tmp
mkdir sandbox
cd sandbox

pwd

cp ../../dataplatform-1.0.0-cr2-runner.jar .

pwd

ls -ltr

jar -xvf ./dataplatform-1.0.0-cr2-runner.jar

cp -r oauth/* ../release-1.0.0-cr2/oauth
cp -r com/appgallabs/dataplatform/util/JsonUtil.class ../com/appgallabs/dataplatform/util
cp -r com/appgallabs/dataplatform/util/Util.class ../com/appgallabs/dataplatform/util
cp -r com/appgallabs/dataplatform/ingestion/util/JobManagerUtil.class ../com/appgallabs/dataplatform/ingestion/util
cp -r com/appgallabs/dataplatform/client/sdk/api/* ../com/appgallabs/dataplatform/client/sdk/api
cp -r com/appgallabs/dataplatform/client/sdk/infrastructure/* ../com/appgallabs/dataplatform/client/sdk/infrastructure
cp -r com/appgallabs/dataplatform/client/sdk/network/* ../com/appgallabs/dataplatform/client/sdk/network
cp -r com/appgallabs/dataplatform/client/sdk/service/* ../com/appgallabs/dataplatform/client/sdk/service

cd ..

jar -cvf braineous-dataingestion-sdk-1.0.0-cr2.jar com oauth
jar -tvf braineous-dataingestion-sdk-1.0.0-cr2.jar
cp braineous-dataingestion-sdk-1.0.0-cr2.jar ../tutorials/get-started/lib
cp braineous-dataingestion-sdk-1.0.0-cr2.jar ../tutorials/create-connector/client/lib
cp dataplatform-1.0.0.cr2-runner.jar ../tutorials/create-connector/server/lib
cp braineous-dataingestion-sdk-1.0.0-cr2.jar ../tutorials/datalake/lib
cp dataplatform-1.0.0.cr2-runner.jar ../tutorials/datalake/lib
cp braineous-dataingestion-sdk-1.0.0-cr2.jar ../

rm -rf ./tmp


