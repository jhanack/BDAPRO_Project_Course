#!/bin/bash
set -x
#$1 exasol docker container, $2 deploy mode (cluster/local)
#set up hosts
EXA_CONTAINER=$1
MODE=$2
mkdir tmp
if [ "$MODE" == "local" ]; then
  docker inspect --format='{{.Name}} {{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $(docker ps -q) | sed 's/\///' >tmp/current_hosts_tmp
  awk '{for(i=NF;i>=1;i--) printf "%s ", $i;print ""}' tmp/current_hosts_tmp >tmp/current_hosts
  docker cp tmp/current_hosts ${EXA_CONTAINER}:/
  docker exec -it ${EXA_CONTAINER} bash -c "cat current_hosts >> /etc/hosts"
else
  docker exec -it ${EXA_CONTAINER} bash -c "
  echo \"10.0.1.26 pg-1
10.0.1.28 pg-2
10.0.1.30 pg-3
10.0.1.17 pg-4
10.0.1.3 mdb1
10.0.1.14 mdb2\" >> /etc/hosts"
fi
#copy necessary files
docker exec -it ${EXA_CONTAINER} bash -c "mkdir jdbclib && mkdir system_cfgs"
docker cp ../../../../lib/. ${EXA_CONTAINER}:/jdbclib/
docker cp system_cfgs/ ${EXA_CONTAINER}:/

#download virtual schemas

wget https://github.com/exasol/generic-virtual-schema/releases/download/2.0.0/virtual-schema-dist-9.0.3-generic-2.0.0.jar -O tmp/virtual-schema-dist-9.0.3-generic-2.0.0.jar
wget https://github.com/exasol/postgresql-virtual-schema/releases/download/2.0.0/virtual-schema-dist-9.0.1-postgresql-2.0.0.jar -O tmp/virtual-schema-dist-9.0.1-postgresql-2.0.0.jar
wget https://github.com/exasol/hive-virtual-schema/releases/download/2.0.0/virtual-schema-dist-9.0.1-hive-2.0.0.jar -O tmp/virtual-schema-dist-9.0.1-hive-2.0.0.jar
wget https://github.com/exasol/mysql-virtual-schema/releases/download/2.0.0/virtual-schema-dist-9.0.1-mysql-2.0.0.jar -O tmp/virtual-schema-dist-9.0.1-mysql-2.0.0.jar
wget https://github.com/exasol/exasol-virtual-schema/releases/download/5.0.4/virtual-schema-dist-9.0.3-exasol-5.0.4.jar  -O tmp/virtual-schema-dist-9.0.3-exasol-5.0.4.jar

docker cp tmp/virtual-schema-dist-9.0.3-generic-2.0.0.jar ${EXA_CONTAINER}:/
docker cp tmp/virtual-schema-dist-9.0.1-postgresql-2.0.0.jar ${EXA_CONTAINER}:/
docker cp tmp/virtual-schema-dist-9.0.1-hive-2.0.0.jar ${EXA_CONTAINER}:/
docker cp tmp/virtual-schema-dist-9.0.1-mysql-2.0.0.jar ${EXA_CONTAINER}:/
docker cp tmp/virtual-schema-dist-9.0.3-exasol-5.0.4.jar ${EXA_CONTAINER}:/

#get bucketfs write pass
docker cp ${EXA_CONTAINER}:/exa/etc/EXAConf tmp/EXAConf
WPASS=$(awk '/WritePasswd/{ print $3; }' tmp/EXAConf | base64 -d)
echo "PASS IS $WPASS"

#postgres
docker exec -it ${EXA_CONTAINER} bash -c "curl -v -X PUT -T jdbclib/postgresql-42.2.18.jar http://w:$WPASS@localhost:2580/default/drivers/jdbc/postgres/postgresql-42.2.18.jar"
docker exec -it ${EXA_CONTAINER} bash -c "curl -v -X PUT -T system_cfgs/postgres_settings.cfg http://w:$WPASS@localhost:2580/default/drivers/jdbc/postgres/settings.cfg"

#mariadb
docker exec -it ${EXA_CONTAINER} bash -c "curl -v -X PUT -T jdbclib/mariadb-java-client-2.7.1.jar http://w:$WPASS@localhost:2580/default/drivers/jdbc/mariadb/mariadb-java-client-2.7.1.jar"
docker exec -it ${EXA_CONTAINER} bash -c "curl -v -X PUT -T system_cfgs/mariadb_settings.cfg http://w:$WPASS@localhost:2580/default/drivers/jdbc/mariadb/settings.cfg"

#hive
#docker exec -it ${EXA_CONTAINER} bash -c "wget -O jdbclib/hive-jdbc-3.1.2-standalone.jar https://repo1.maven.org/maven2/org/apache/hive/hive-jdbc/3.1.2/hive-jdbc-3.1.2-standalone.jar"
#docker exec -it ${EXA_CONTAINER} bash -c "curl -v -X PUT -T jdbclib/hive-jdbc-3.1.2-standalone.jar http://w:$WPASS@localhost:2580/default/drivers/jdbc/hive/hive-jdbc-3.1.2-standalone.jar"
docker exec -it ${EXA_CONTAINER} bash -c "curl -v -X PUT -T jdbclib/HiveJDBC41.jar http://w:$WPASS@localhost:2580/default/drivers/jdbc/hive/HiveJDBC41.jar"
docker exec -it ${EXA_CONTAINER} bash -c "curl -v -X PUT -T system_cfgs/hive_settings.cfg http://w:$WPASS@localhost:2580/default/drivers/jdbc/hive/settings.cfg"

#exasol
docker exec -it ${EXA_CONTAINER} bash -c "curl -v -X PUT -T jdbclib/exajdbc.jar http://w:$WPASS@localhost:2580/default/drivers/jdbc/exasol/exajdbc.jar"
docker exec -it ${EXA_CONTAINER} bash -c "curl -v -X PUT -T system_cfgs/exa_settings.cfg http://w:$WPASS@localhost:2580/default/drivers/jdbc/exasol/settings.cfg"

#exasol virtual schema
docker exec -it ${EXA_CONTAINER} bash -c "curl -v -X PUT -T virtual-schema-dist-9.0.3-generic-2.0.0.jar http://w:$WPASS@localhost:2580/default/virtualschemas/virtual-schema-dist-9.0.3-generic-2.0.0.jar"
docker exec -it ${EXA_CONTAINER} bash -c "curl -v -X PUT -T virtual-schema-dist-9.0.1-postgresql-2.0.0.jar http://w:$WPASS@localhost:2580/default/virtualschemas/virtual-schema-dist-9.0.1-postgresql-2.0.0.jar"
docker exec -it ${EXA_CONTAINER} bash -c "curl -v -X PUT -T virtual-schema-dist-9.0.1-hive-2.0.0.jar http://w:$WPASS@localhost:2580/default/virtualschemas/virtual-schema-dist-9.0.1-hive-2.0.0.jar"
docker exec -it ${EXA_CONTAINER} bash -c "curl -v -X PUT -T virtual-schema-dist-9.0.1-mysql-2.0.0.jar http://w:$WPASS@localhost:2580/default/virtualschemas/virtual-schema-dist-9.0.1-mysql-2.0.0.jar"
docker exec -it ${EXA_CONTAINER} bash -c "curl -v -X PUT -T virtual-schema-dist-9.0.3-exasol-5.0.4.jar http://w:$WPASS@localhost:2580/default/virtualschemas/virtual-schema-dist-9.0.3-exasol-5.0.4.jar"



#increase memory size
docker exec -it ${EXA_CONTAINER} bash -c "exaconf modify-volume -n DataVolume1 -s 100GiB"
#docker exec -it ${EXA_CONTAINER} bash -c "sed -i '/Checksum =/c\ Checksum = COMMIT' /exa/etc/EXAConf"
#docker exec -it ${EXA_CONTAINER} bash -c "exaconf commit"

#check that everything is uploaded
#docker exec -it ${EXA_CONTAINER} bash -c "exaconf commit"
#curl -v http://r@localhost:2580/default/


#clean up
rm -r tmp
