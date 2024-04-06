export HADOOP_HOME=~/mumma/braineous/infrastructure/hadoop-3.3.6
export HIVE_HOME=~/mumma/braineous/infrastructure/apache-hive-3.1.3-bin
export HADOOP_CLASSPATH=$HIVE_HOME/conf:$HIVE_HOME/lib

~/mumma/braineous/infrastructure/apache-hive-3.1.3-bin/bin/hive --service metastore