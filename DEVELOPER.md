## Prepare Latest Release

    * top-level directory: ./prepare-release-1.0.0-cr3.sh


## Upgrade data_applications/data_replication

Until Branieous libraries are added to the central maven repository (in progress)
To upgrade data applications

    * top-level directory:  
    cp braineous-dataingestion-sdk-1.0.0-cr3.jar 
    /Users/babyboy/mumma/braineous/data_platform/data_applications/1_0/braineous_data_applications/data_applications/data_replication/lib

    * cd to data_replication directory locally
    ./build.sh