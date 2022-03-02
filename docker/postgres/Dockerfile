FROM postgres:12.10-bullseye

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        gcc \
        libc-dev \
        make \
        cmake \
        ca-certificates \
        git \
        postgresql-12 \
        postgresql-server-dev-12 \
        odbc-postgresql \
        unixodbc-dev \
        libssl-dev \
##        curl \
##        unzip \
##        zip \
        openjdk-11-jdk \
    && rm -rf /var/lib/apt/lists/*

RUN git clone https://github.com/CartoDB/odbc_fdw.git && cd odbc_fdw && make && make install

RUN ln -s /usr/lib/jvm/java-11-openjdk-amd64/lib/server/libjvm.so /usr/lib/libjvm.so
RUN git clone https://github.com/atris/JDBC_FDW.git \
&& cd JDBC_FDW \
&& make install USE_PGXS=1