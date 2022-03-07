GRANT ALL PRIVILEGES ON *.* TO 'bdapro_user'@'%';
FLUSH PRIVILEGES;

CREATE TABLE sf10_supplier
(
    s_suppkey   INTEGER,
    s_name      CHAR(25),
    s_address   VARCHAR(40),
    s_nationkey INTEGER,
    s_phone     CHAR(15),
    s_acctbal   NUMERIC,
    s_comment   VARCHAR(101)
);

CREATE TABLE sf10_part
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

CREATE TABLE sf10_partsupp
(
    ps_partkey    INTEGER,
    ps_suppkey    INTEGER,
    ps_availqty   INTEGER,
    ps_supplycost NUMERIC,
    ps_comment    VARCHAR(199)
);

CREATE TABLE sf10_customer
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

CREATE TABLE sf10_orders
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

CREATE TABLE sf10_lineitem
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

CREATE TABLE sf10_nation
(
    n_nationkey INTEGER,
    n_name      CHAR(25),
    n_regionkey INTEGER,
    n_comment   VARCHAR(152)
);

CREATE TABLE sf10_region
(
    r_regionkey INTEGER,
    r_name      CHAR(25),
    r_comment   VARCHAR(152)
);

USE bdapro_database;

LOAD DATA LOCAL INFILE '/docker-entrypoint-initdb.d/tpch/sf10/customer.tbl' into table sf10_customer FIELDS TERMINATED BY '|';
LOAD DATA LOCAL INFILE '/docker-entrypoint-initdb.d/tpch/sf10/orders.tbl' into table sf10_orders FIELDS TERMINATED BY '|';
LOAD DATA LOCAL INFILE '/docker-entrypoint-initdb.d/tpch/sf10/lineitem.tbl' into table sf10_lineitem FIELDS TERMINATED BY '|';
LOAD DATA LOCAL INFILE '/docker-entrypoint-initdb.d/tpch/sf10/nation.tbl' into table sf10_nation FIELDS TERMINATED BY '|';
LOAD DATA LOCAL INFILE '/docker-entrypoint-initdb.d/tpch/sf10/partsupp.tbl' into table sf10_partsupp FIELDS TERMINATED BY '|';
LOAD DATA LOCAL INFILE '/docker-entrypoint-initdb.d/tpch/sf10/part.tbl' into table sf10_part FIELDS TERMINATED BY '|';
LOAD DATA LOCAL INFILE '/docker-entrypoint-initdb.d/tpch/sf10/region.tbl' into table sf10_region FIELDS TERMINATED BY '|';
LOAD DATA LOCAL INFILE '/docker-entrypoint-initdb.d/tpch/sf10/supplier.tbl' into table sf10_supplier FIELDS TERMINATED BY '|';
