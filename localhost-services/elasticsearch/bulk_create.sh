curl -v -u elastic:password -X POST "http://localhost:9200/_bulk?pretty" -H 'Content-Type: application/json' -d'
{ "index" : { "_index" : "educative"} }
{ "articleName" : "elasticsearch-intro" }
{ "index" : { "_index" : "educative"} }
{ "articleName" : "elasticsearch-insert-data" }
{ "index" : { "_index" : "educative"} }
{ "articleName" : "elasticsearch-query-data" }
'