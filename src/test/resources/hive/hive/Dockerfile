FROM hadoop-base

ENV HIVE_VERSION 3.1.2
ENV SPARK_VERSION=2.3.0
ENV HADOOP_VERSION=2.7

ENV HIVE_URL https://archive.apache.org/dist/hive/hive-$HIVE_VERSION/apache-hive-$HIVE_VERSION-bin.tar.gz
ENV HIVE_HOME /opt/hive
ENV SPARK_HOME /opt/spark
ENV PATH $HIVE_HOME/bin:$PATH
ENV PATH $SPARK_HOME/bin/:$PATH

ENV PG_URL https://jdbc.postgresql.org/download/postgresql-42.2.16.jar
ENV MDB_URL https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/2.6.0/mariadb-java-client-2.6.0.jar
ENV EXA_URL https://maven.exasol.com/artifactory/exasol-releases/com/exasol/exasol-jdbc/7.0.0/exasol-jdbc-7.0.0.jar
ENV SPARK_URL https://archive.apache.org/dist/spark/spark-$SPARK_VERSION/spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION.tgz

RUN apt-get update && apt-get install nano

RUN curl -O https://dist.apache.org/repos/dist/release/hive/KEYS
RUN gpg --import KEYS

RUN set -x \
    && curl -fSL "$HIVE_URL" -o /tmp/hive.tar.gz \
    && curl -fSL "$HIVE_URL.asc" -o /tmp/hive.tar.gz.asc \
    && gpg --verify /tmp/hive.tar.gz.asc \
    && tar -xvf /tmp/hive.tar.gz -C /opt/ \
    && mv /opt/apache-hive-$HIVE_VERSION-bin /opt/hive \
    && rm /tmp/hive.tar.gz*

RUN set -x \
    && curl -fSL "$SPARK_URL" -o /tmp/spark.tgz \
#    && curl -fSL "$SPARK_URL.asc" -o /tmp/spark.tgz.asc \
#    && gpg --verify /tmp/spark.tgz.asc \
    && tar -xvzf /tmp/spark.tgz -C /opt/ \
    && mv /opt/spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION /opt/spark
#    && rm /tmp/spark.tgz*

RUN curl -fSL "$PG_URL" -o $HIVE_HOME/lib/postgresql-jdbc.jar
RUN curl -fSL "$MDB_URL" -o $HIVE_HOME/lib/mariadb-jdbc.jar
RUN curl -fSL "$EXA_URL" -o $HIVE_HOME/lib/exasol-jdbc-7.0.0.jar

#Custom configuration goes here
COPY conf/hive-site.xml $HIVE_HOME/conf
COPY conf/beeline-log4j2.properties $HIVE_HOME/conf
COPY conf/hive-env.sh $HIVE_HOME/conf
COPY conf/hive-exec-log4j2.properties $HIVE_HOME/conf
COPY conf/hive-log4j2.properties $HIVE_HOME/conf
COPY conf/ivysettings.xml $HIVE_HOME/conf
COPY conf/llap-daemon-log4j2.properties $HIVE_HOME/conf

COPY create_tpch_sf1.sql /
COPY import_tpch_sf1.sql /

EXPOSE 10000
EXPOSE 10002

COPY entrypoint.sh /entrypoint.sh
RUN chmod a+x /entrypoint.sh

COPY run-server.sh /run-server.sh
RUN chmod a+x /run-server.sh

COPY run-metastore.sh /run-metastore.sh
RUN chmod a+x /run-metastore.sh

ENTRYPOINT ["/entrypoint.sh"]