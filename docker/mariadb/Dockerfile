FROM mariadb:10.5-focal

RUN apt-get update && apt install openjdk-8-jdk -y && export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
#RUN apt-get remove --purge mariadb-server-10.5 -y && apt-get autoremove -y && apt-get autoclean -y
RUN apt-get update && apt-get install mariadb-plugin-connect wget -y
RUN apt-get update && apt-get install libodbc1 unixodbc-dev libssl-dev odbc-postgresql curl alien -y

# comment out a few problematic configuration values
#RUN	find /etc/mysql/ -name '*.cnf' -print0 \
#		| xargs -0 grep -lZE '^(bind-address|log|user\s)' \
#		| xargs -rt -0 sed -Ei 's/^(bind-address|log|user\s)/#&/';
# don't reverse lookup hostnames, they are usually another container
#RUN	echo '[mysqld]\nskip-host-cache\nskip-name-resolve' > /etc/mysql/conf.d/docker.cnf


#RUN apt-get update && apt-get install software-properties-common mariadb-plugin-connect -y

#https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-common/2.7.7/hadoop-common-2.7.7.jar
#https://repo1.maven.org/maven2/org/apache/hive/hive-jdbc/2.3.8/hive-jdbc-2.3.8-standalone.jar
#https://repo.hortonworks.com/content/repositories/releases/org/apache/hive/hive-jdbc/2.3.8/hive-jdbc-2.3.8.jar
#https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-auth/2.7.7/hadoop-auth-2.7.7.jar
#https://downloads.cloudera.com/connectors/ClouderaHiveJDBC-2.6.13.1016.zip


#COPY ./jars/* /usr/lib/mysql/plugin/
RUN wget https://jira.mariadb.org/secure/attachment/44179/JavaWrappers.jar -O /usr/lib/mysql/plugin/JavaWrappers.jar
COPY ./lib/* /tmp/
COPY ./connect.cnf /etc/mysql/mariadb.conf.d/connect.cnf

RUN chmod 0444 /etc/mysql/mariadb.conf.d/connect.cnf
#COPY privileges.sql /docker-entrypoint-initdb.d/
#COPY ./import/setupMariaDB1.sql /docker-entrypoint-initdb.d/

COPY ./my.cnf /etc/mysql/mariadb.conf.d/