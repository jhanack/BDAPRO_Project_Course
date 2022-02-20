#!/bin/bash

#. /usr/opt/EXASuite-7/EXAClusterOS-7.1.6/docker/entrypoint.sh init-sc

WPASS=$(awk '/WritePasswd/{ print $3; }' /exa/etc/EXAConf | base64 -d)
echo "PASS IS $WPASS"

curl -v -X PUT -T jdbclib/postgresql-42.2.18.jar http://w:$WPASS@localhost:2580/default/drivers/jdbc/postgres/postgresql-42.2.18.jar
curl -v -X PUT -T system_cfgs/postgres_settings.cfg http://w:$WPASS@localhost:2580/default/drivers/jdbc/postgres/settings.cfg

curl -v -X PUT -T jdbclib/mariadb-java-client-2.7.1.jar http://w:$WPASS@localhost:2580/default/drivers/jdbc/mariadb/mariadb-java-client-2.7.1.jar
curl -v -X PUT -T system_cfgs/mariadb_settings.cfg http://w:$WPASS@localhost:2580/default/drivers/jdbc/mariadb/settings.cfg

curl -v -X PUT -T jdbclib/HiveJDBC41.jar http://w:$WPASS@localhost:2580/default/drivers/jdbc/hive/HiveJDBC41.jar
curl -v -X PUT -T system_cfgs/hive_settings.cfg http://w:$WPASS@localhost:2580/default/drivers/jdbc/hive/settings.cfg

curl -v -X PUT -T jdbclib/exajdbc.jar http://w:$WPASS@localhost:2580/default/drivers/jdbc/exasol/exajdbc.jar
curl -v -X PUT -T system_cfgs/exa_settings.cfg http://w:$WPASS@localhost:2580/default/drivers/jdbc/exasol/settings.cfg

curl -v -X PUT -T virtual-schema-dist-9.0.3-generic-2.0.0.jar http://w:$WPASS@localhost:2580/default/virtualschemas/virtual-schema-dist-9.0.3-generic-2.0.0.jar
curl -v -X PUT -T virtual-schema-dist-9.0.1-postgresql-2.0.0.jar http://w:$WPASS@localhost:2580/default/virtualschemas/virtual-schema-dist-9.0.1-postgresql-2.0.0.jar
curl -v -X PUT -T virtual-schema-dist-9.0.1-hive-2.0.0.jar http://w:$WPASS@localhost:2580/default/virtualschemas/virtual-schema-dist-9.0.1-hive-2.0.0.jar
curl -v -X PUT -T virtual-schema-dist-9.0.1-mysql-2.0.0.jar http://w:$WPASS@localhost:2580/default/virtualschemas/virtual-schema-dist-9.0.1-mysql-2.0.0.jar
curl -v -X PUT -T virtual-schema-dist-9.0.3-exasol-5.0.4.jar http://w:$WPASS@localhost:2580/default/virtualschemas/virtual-schema-dist-9.0.3-exasol-5.0.4.jar



