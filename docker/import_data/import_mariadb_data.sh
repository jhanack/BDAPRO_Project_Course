#!/bin/bash
set -x
docker exec mdb1 bash -c "mysql mdb < /import/create_tpch_sf1.sql -u mariadb -p123456"
docker exec mdb1 bash -c "mysql mdb < /import/import_tpch_sf1.sql -u mariadb -p123456"

docker exec mdb1 bash -c "mysql mdb < /import/create_tpch_sf10.sql -u mariadb -p123456"
docker exec mdb1 bash -c "mysql mdb < /import/import_tpch_sf10.sql -u mariadb -p123456"
