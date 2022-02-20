FROM postgres:13

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        gcc \
        libc-dev \
        make \
        cmake \
        ca-certificates \
        git \
        postgresql-server-dev-13 \
        odbc-postgresql \
        unixodbc-dev \
        libssl-dev \
        alien \
        curl \
        nano \
        openjdk-11-jdk \
    && rm -rf /var/lib/apt/lists/*

RUN git clone https://github.com/CartoDB/odbc_fdw.git && cd odbc_fdw && make && make install

RUN git clone https://github.com/MariaDB/mariadb-connector-odbc.git
RUN mkdir build && cd build && cmake ../mariadb-connector-odbc/ -DCMAKE_BUILD_TYPE=RelWithDebInfo -DCONC_WITH_UNIT_TESTS=Off -DCMAKE_INSTALL_PREFIX=/usr/local -DWITH_SSL=OPENSSL \
    && cmake --build . --config RelWithDebInfo \
    && make install

COPY config/mariadb.ini /
RUN cat mariadb.ini >> /etc/odbcinst.ini

RUN curl -O https://downloads.cloudera.com/connectors/hive_odbc_2.6.1.1001/Linux/ClouderaHiveODBC-2.6.1.1001-1.x86_64.rpm
RUN alien -i ClouderaHiveODBC-2.6.1.1001-1.x86_64.rpm
COPY config/hive.ini /
RUN cat hive.ini >> /etc/odbcinst.ini

RUN ln -s /usr/lib/jvm/java-11-openjdk-amd64/lib/server/libjvm.so /usr/lib/libjvm.so
RUN git clone https://github.com/atris/JDBC_FDW.git \
 && cd JDBC_FDW \
 && make install USE_PGXS=1
