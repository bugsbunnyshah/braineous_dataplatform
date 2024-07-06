-----
If Apache Iceberg is not setup locally on Docker

---
Step 1: git clone https://github.com/tabular-io/docker-spark-iceberg.git

Step 2: cd docker-spark-iceberg

Step 3: docker compose up

Step4: docker exec -it spark-iceberg spark-sql

----
If Iceberg is setup

---

Step 1: ./start_iceberg.sh

Step 2: ./start_sparql.sh

----

Test via Sparql

---

CREATE TABLE demo.nyc.taxis
(
vendor_id bigint,
trip_id bigint,
trip_distance float,
fare_amount double,
store_and_fwd_flag string
)
PARTITIONED BY (vendor_id);

---

INSERT INTO demo.nyc.taxis
VALUES (1, 1000371, 1.8, 15.32, 'N'), (2, 1000372, 2.5, 22.15, 'N'), (2, 1000373, 0.9, 9.01, 'N'), (1, 1000374, 8.4, 42.13, 'Y');

---

SELECT * FROM demo.nyc.taxis;

