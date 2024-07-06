curl -v -u elastic:password -X POST "http://localhost:9200/educative/_doc/?pretty" -H 'Content-Type: application/json' -d'
{ "articleName" : "elasticsearch-intro" }
'