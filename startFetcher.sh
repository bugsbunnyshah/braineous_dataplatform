curl --location --request POST 'http://localhost:8080/dataIngester/fetch/' \
--header 'Principal: PAlDekAoo0XWjAicU9SQDKgy7B0y2p2t' \
--header 'Bearer: bearer' \
--header 'Content-Type: application/json' \
--data-raw '{
  "agentId": "ian",
  "entity": "flight"
}'