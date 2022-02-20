FROM exasol/docker-db:latest

RUN yum -y install curl wget

RUN mkdir jdbckib
RUN mkdir system_cfgs

COPY lib/. /jdbclib/
COPY system_cfgs/. /system_cfgs/

RUN wget https://github.com/exasol/generic-virtual-schema/releases/download/2.0.0/virtual-schema-dist-9.0.3-generic-2.0.0.jar -O /virtual-schema-dist-9.0.3-generic-2.0.0.jar
RUN wget https://github.com/exasol/postgresql-virtual-schema/releases/download/2.0.0/virtual-schema-dist-9.0.1-postgresql-2.0.0.jar -O /virtual-schema-dist-9.0.1-postgresql-2.0.0.jar
RUN wget https://github.com/exasol/hive-virtual-schema/releases/download/2.0.0/virtual-schema-dist-9.0.1-hive-2.0.0.jar -O /virtual-schema-dist-9.0.1-hive-2.0.0.jar
RUN wget https://github.com/exasol/mysql-virtual-schema/releases/download/2.0.0/virtual-schema-dist-9.0.1-mysql-2.0.0.jar -O /virtual-schema-dist-9.0.1-mysql-2.0.0.jar
RUN wget https://github.com/exasol/exasol-virtual-schema/releases/download/5.0.4/virtual-schema-dist-9.0.3-exasol-5.0.4.jar  -O /virtual-schema-dist-9.0.3-exasol-5.0.4.jar

#RUN mkdir -p /exa/data/bucketfs/bfsdefault/default/drivers/jdbc/postgres
#RUN mkdir -p /exa/data/bucketfs/bfsdefault/default/virtualschemas

#RUN mv /jdbclib/postgresql-42.2.18.jar /exa/data/bucketfs/bfsdefault/default/drivers/jdbc/postgres/postgresql-42.2.18.jar
#RUN mv /system_cfgs/postgres_settings.cfg /exa/data/bucketfs/bfsdefault/default/drivers/jdbc/postgres/settings.cfg

#RUN mv virtual-schema-dist-9.0.3-generic-2.0.0.jar /exa/data/bucketfs/bfsdefault/default/virtualschemas/virtual-schema-dist-9.0.3-generic-2.0.0.jar
#RUN mv virtual-schema-dist-9.0.1-postgresql-2.0.0.jar /exa/data/bucketfs/bfsdefault/default/virtualschemas/virtual-schema-dist-9.0.1-postgresql-2.0.0.jar

COPY setup.sh /setup.sh
RUN chmod a+rx /setup.sh

#ENTRYPOINT ["/setup.sh"]