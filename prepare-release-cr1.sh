rm -rf release-cr1

mkdir release-cr1
cd release-cr1
mkdir braineous-1.0.0-cr1
cd braineous-1.0.0-cr1
mkdir bin
mkdir tutorials
cd tutorials
mkdir get-started


cd ..
cd ..
cd ..

cp -r releases/braineous-1.0.0-cr1/bin/* release-cr1/braineous-1.0.0-cr1/bin
cp -r tutorials/get-started/* release-cr1/braineous-1.0.0-cr1/tutorials/get-started

pwd


