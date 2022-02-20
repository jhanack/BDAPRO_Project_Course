curl -O https://www.exasol.com/support/secure/attachment/153747/EXAplus-7.1.rc1.tar.gz
mkdir exaplus && tar zxvf EXAplus-7.1.rc1.tar.gz -C exaplus


exaconf add-bucketfs --name newbucketfs --http-port 6932 --https-port 0 --owner 500:500
exaconf add-bucket --name newbucketfs-bucket --bfs-name newbucketfs --read-passwd $(echo -n "newread" | base64) --write-passwd $(echo -n "newwrite" | base64)
sed -i '/Checksum =/c\ Checksum = COMMIT' /exa/etc/EXAConf
exaconf commit

#download jars

wget https://github.com/exasol/generic-virtual-schema/releases/download/2.0.0/virtual-schema-dist-9.0.3-generic-2.0.0.jar
wget https://github.com/exasol/postgresql-virtual-schema/releases/download/2.0.0/virtual-schema-dist-9.0.1-postgresql-2.0.0.jar
wget https://jdbc.postgresql.org/download/postgresql-42.2.18.jar


# upload jars to bucketfs
#readpass vWZafC4tTyd0zpeZryrFct
#writepass pCZrbXwhwEe314tT5BNHLS


curl -v -X PUT -T postgresql-42.2.18.jar http://w:pCZrbXwhwEe314tT5BNHLS@localhost:2580/default/drivers/jdbc/postgres/test.jar
curl -v -X PUT -T system.cfg http://w:pCZrbXwhwEe314tT5BNHLS@localhost:2580/default/drivers/jdbc/postgres/system.cfg
curl -v -X PUT -T virtual-schema-dist-9.0.3-generic-2.0.0.jar http://w:pCZrbXwhwEe314tT5BNHLS@localhost:2580/default/virtualschemas/virtual-schema-dist-9.0.3-generic-2.0.0.jar
curl -v -X PUT -T virtual-schema-dist-9.0.3-generic-2.0.0.jar http://w:pCZrbXwhwEe314tT5BNHLS@localhost:2580/default/virtualschemas/virtual-schema-dist-9.0.1-postgresql-2.0.0.jar

curl -v http://r:2zwJ6VNqGvd82YzUNYIbT6@localhost:2580/default/

curl -X PUT -T virtual-schema-dist-9.0.3-generic-2.0.0.jar http://w:igAOxo606VcFjTIMaE0oxA@localhost:2580/default/virtual-schema-dist-9.0.3-generic-2.0.0.jar
curl -X PUT -T virtual-schema-dist-9.0.3-generic-2.0.0.jar http://w:newwrite@localhost:6932/newbucketfs-bucket/virtual-schema-dist-9.0.3-generic-2.0.0.jar
curl -X PUT -T postgresql-42.2.18.jar http://w:newwrite@localhost:6932/newbucketfs-bucket/postgresql-42.2.18.jar
curl -X PUT -T virtual-schema-dist-9.0.3-generic-2.0.0.jar http://w:newwrite@localhost:6932/newbucketfs-bucket/virtual-schema-dist-9.0.1-postgresql-2.0.0.jar

docker exec -it exa1 bash -c "curl -X DELETE http://w:$WPASS@localhost:2580/default/drivers/jdbc/hive/system.cfg"

