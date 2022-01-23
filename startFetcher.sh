curl --location --request POST 'http://localhost/dataIngester/fetch/' \
--header 'Principal: PAlDekAoo0XWjAicU9SQDKgy7B0y2p2t' \
--header 'Bearer: bearer' \
--header 'Content-Type: application/json' \
--header 'Cookie: authentik_csrf=mobQCxHGKsIbdXS0AFmigH5yJQ2URucYpwumrxBl5FVeLiwBQ68nkRa7RnPfBr5q' \
--data-raw '{
  "agentId": "ian",
  "entity": "flight"
}'