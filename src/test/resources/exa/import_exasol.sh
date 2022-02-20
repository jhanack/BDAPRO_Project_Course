#!/bin/bash
set -x
#1: exasol container $2: internal exasol address,
EXA_CONTAINER=$1
#EXA_ADDRESS=$2

wget https://www.exasol.com/support/secure/attachment/153747/EXAplus-7.1.rc1.tar.gz -O EXAplus-7.1.rc1.tar.gz

tar zxvf EXAplus-7.1.rc1.tar.gz && mv EXAplus-7.1.rc1 exaplus

#./exaplus -c $EXA_ADDRESS -u sys -p exasol -f ../exasol_create_table_sf1.sql
#./exaplus -c $EXA_ADDRESS -u sys -p exasol -f ../exasol_create_table_sf10.sql
#./exaplus -c $EXA_ADDRESS -u sys -p exasol -f ../load_tables.sql

docker cp exaplus/ ${EXA_CONTAINER}:/
docker cp exasol_create_table_sf10.sql ${EXA_CONTAINER}:/
docker cp exasol_create_table_sf1.sql ${EXA_CONTAINER}:/
docker cp load_tables.sql ${EXA_CONTAINER}:/

docker exec -it ${EXA_CONTAINER} bash -c "cd exaplus && ./exaplus -c localhost:8563 -u sys -p exasol -f /exasol_create_table_sf1.sql"
docker exec -it ${EXA_CONTAINER} bash -c "cd exaplus && ./exaplus -c localhost:8563 -u sys -p exasol -f /exasol_create_table_sf10.sql"
docker exec -it ${EXA_CONTAINER} bash -c "cd exaplus && ./exaplus -c localhost:8563 -u sys -p exasol -f /load_tables.sql"

rm EXAplus-7.1.rc1.tar.gz
rm -r exaplus