#!/bin/bash
set -x
docker exec hive-server bash -c "hive -f create_tpch_sf10.sql"
docker exec hive-server bash -c "hive -f import_tpch_sf10.sql"
