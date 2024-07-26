# braineous-tutorials
Tutorials for Braineous Data Platform

{
"pipeId": "mysql_mongodb_fan_out_to_target",
"configuration": [
{
"storeDriver": "com.appgallabs.dataplatform.targetSystem.core.driver.MongoDBStoreDriver",
"name": "scenario1_store_mongodb",
"config": {
"connectionString": "mongodb://localhost:27017",
"database": "scenario1_store",
"collection": "data",
"jsonpathExpressions": []
}
},
{
"storeDriver": "com.appgallabs.dataplatform.tutorial.MySqlStoreDriver",
"name": "scenario1_store_mysql",
"config": {
"connectionString": "jdbc:mysql://localhost:3306/braineous_staging_database",
"username": "root",
"password": "",
"jsonpathExpressions": []
}
}
]
}

