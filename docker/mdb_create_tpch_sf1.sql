CREATE TABLE sf1_supplier
(
    s_suppkey   INTEGER,
    s_name      CHAR(25),
    s_address   VARCHAR(40),
    s_nationkey INTEGER,
    s_phone     CHAR(15),
    s_acctbal   NUMERIC,
    s_comment   VARCHAR(101)
);

CREATE TABLE sf1_part
(
    p_partkey     INTEGER,
    p_name        VARCHAR(55),
    p_mfgr        CHAR(25),
    p_brand       CHAR(10),
    p_type        VARCHAR(25),
    p_size        INTEGER,
    p_container   CHAR(10),
    p_retailprice NUMERIC,
    p_comment     VARCHAR(23)
);

CREATE TABLE sf1_partsupp
(
    ps_partkey    INTEGER,
    ps_suppkey    INTEGER,
    ps_availqty   INTEGER,
    ps_supplycost NUMERIC,
    ps_comment    VARCHAR(199)
);

CREATE TABLE sf1_customer
(
    c_custkey    INTEGER,
    c_name       VARCHAR(25),
    c_address    VARCHAR(40),
    c_nationkey  INTEGER,
    c_phone      CHAR(15),
    c_acctbal    DECIMAL(20,2),
    c_mktsegment CHAR(10),
    c_comment    VARCHAR(117)
);

CREATE TABLE sf1_orders
(
    o_orderkey      BIGINT,
    o_custkey       INTEGER,
    o_orderstatus   CHAR(1),
    o_totalprice    NUMERIC,
    o_orderdate     DATE,
    o_orderpriority CHAR(15),
    o_clerk         CHAR(15),
    o_shippriority  INTEGER,
    o_comment       VARCHAR(79)
);

CREATE TABLE sf1_lineitem
(
    l_orderkey      BIGINT,
    l_partkey       INTEGER,
    l_suppkey       INTEGER,
    l_linenumber    INTEGER,
    l_quantity      DECIMAL(12, 2),
    l_extendedprice DECIMAL(12, 2),
    l_discount      DECIMAL(12, 2),
    l_tax           DECIMAL(12, 2),
    l_returnflag    CHAR(1),
    l_linestatus    CHAR(1),
    l_shipdate      DATE,
    l_commitdate    DATE,
    l_receiptdate   DATE,
    l_shipinstruct  CHAR(25),
    l_shipmode      CHAR(10),
    l_comment       VARCHAR(44)
);

CREATE TABLE sf1_nation
(
    n_nationkey INTEGER,
    n_name      CHAR(25),
    n_regionkey INTEGER,
    n_comment   VARCHAR(152)
);

CREATE TABLE sf1_region
(
    r_regionkey INTEGER,
    r_name      CHAR(25),
    r_comment   VARCHAR(152)
);

USE bdapro_database;
LOAD DATA LOCAL INFILE '/docker-entrypoint-initdb.d/COVID-19_Hospital_Capacity.csv' into TABLE bdapro_schema COLUMNS TERMINATED BY ',' IGNORE 1 LINES;

LOAD DATA LOCAL INFILE '/data/sf1/customer.tbl' into table mdb_sf1_customer FIELDS TERMINATED BY '|';
LOAD DATA LOCAL INFILE '/data/sf1/orders.tbl' into table mdb_sf1_orders FIELDS TERMINATED BY '|';
LOAD DATA LOCAL INFILE '/data/sf1/lineitem.tbl' into table mdb_sf1_lineitem FIELDS TERMINATED BY '|';
LOAD DATA LOCAL INFILE '/data/sf1/nation.tbl' into table mdb_sf1_nation FIELDS TERMINATED BY '|';
LOAD DATA LOCAL INFILE '/data/sf1/partsupp.tbl' into table mdb_sf1_partsupp FIELDS TERMINATED BY '|';
LOAD DATA LOCAL INFILE '/data/sf1/part.tbl' into table mdb_sf1_part FIELDS TERMINATED BY '|';
LOAD DATA LOCAL INFILE '/data/sf1/region.tbl' into table mdb_sf1_region FIELDS TERMINATED BY '|';
LOAD DATA LOCAL INFILE '/data/sf1/supplier.tbl' into table mdb_sf1_supplier FIELDS TERMINATED BY '|';

\COPY sf1_lineitem FROM /var/lib/postgresql/tpch/sf1/lineitem.tbl with CSV DELIMITER '|' QUOTE '"' ESCAPE '\';
\COPY sf1_supplier FROM /var/lib/postgresql/tpch/sf1/supplier.tbl with CSV DELIMITER '|' QUOTE '"' ESCAPE '\';
\COPY sf1_region FROM /var/lib/postgresql/tpch/sf1/region.tbl with CSV DELIMITER '|' QUOTE '"' ESCAPE '\';
\COPY sf1_nation FROM /var/lib/postgresql/tpch/sf1/nation.tbl with CSV DELIMITER '|' QUOTE '"' ESCAPE '\';
\COPY sf1_orders FROM /var/lib/postgresql/tpch/sf1/orders.tbl with CSV DELIMITER '|' QUOTE '"' ESCAPE '\';
\COPY sf1_customer FROM /var/lib/postgresql/tpch/sf1/customer.tbl with CSV DELIMITER '|' QUOTE '"' ESCAPE '\';
\COPY sf1_partsupp FROM /var/lib/postgresql/tpch/sf1/partsupp.tbl with CSV DELIMITER '|' QUOTE '"' ESCAPE '\';
\COPY sf1_part FROM /var/lib/postgresql/tpch/sf1/part.tbl with CSV DELIMITER '|' QUOTE '"' ESCAPE '\';