CREATE SCHEMA IF NOT EXISTS "tpch";
CREATE TABLE "exa_sf10_nation"
(
    "n_nationkey" DECIMAL(18, 0) NOT NULL ENABLE,
    "n_name"      CHAR(25) ASCII NOT NULL ENABLE,
    "n_regionkey" DECIMAL(18, 0) NOT NULL ENABLE,
    "n_comment"   VARCHAR(152) ASCII
);

CREATE TABLE "exa_sf10_region"
(
    "r_regionkey" DECIMAL(18, 0) NOT NULL ENABLE,
    "r_name"      CHAR(25) ASCII NOT NULL ENABLE,
    "r_comment"   VARCHAR(152) ASCII
);

CREATE TABLE "exa_sf10_part"
(
    "p_partkey"     DECIMAL(18, 0) NOT NULL ENABLE,
    "p_name"        VARCHAR(55) ASCII NOT NULL ENABLE,
    "p_mfgr"        CHAR(25) ASCII NOT NULL ENABLE,
    "p_brand"       CHAR(10) ASCII NOT NULL ENABLE,
    "p_type"        VARCHAR(25) ASCII NOT NULL ENABLE,
    "p_size"        DECIMAL(10, 0) NOT NULL ENABLE,
    "p_container"   CHAR(10) ASCII NOT NULL ENABLE,
    "p_retailprice" DECIMAL(12, 2) NOT NULL ENABLE,
    "p_comment"     VARCHAR(23) ASCII NOT NULL ENABLE
);

CREATE TABLE "exa_sf10_supplier"
(
     "s_suppkey"   DECIMAL(18, 0) NOT NULL ENABLE,
     "s_name"      CHAR(25) ASCII NOT NULL ENABLE,
     "s_address"   VARCHAR(40) ASCII NOT NULL ENABLE,
     "s_nationkey" DECIMAL(18, 0) NOT NULL ENABLE,
     "s_phone"     CHAR(15) ASCII NOT NULL ENABLE,
     "s_acctbal"   DECIMAL(12, 2) NOT NULL ENABLE,
     "s_comment"   VARCHAR(101) ASCII NOT NULL ENABLE
);

CREATE TABLE "exa_sf10_partsupp"
(
    "ps_partkey"   DECIMAL(18, 0) NOT NULL ENABLE,
    "ps_suppkey"    DECIMAL(18, 0) NOT NULL ENABLE,
    "ps_availqty"   DECIMAL(18, 0) NOT NULL ENABLE,
    "ps_supplycost" DECIMAL(12, 2) NOT NULL ENABLE,
    "ps_comment"    VARCHAR(199) ASCII NOT NULL ENABLE
);

CREATE TABLE "exa_sf10_customer"
(
     "c_custkey"    DECIMAL(18, 0) NOT NULL ENABLE,
     "c_name"       VARCHAR(25) ASCII NOT NULL ENABLE,
     "c_address"    VARCHAR(40) ASCII NOT NULL ENABLE,
     "c_nationkey"  DECIMAL(18, 0) NOT NULL ENABLE,
     "c_phone"      CHAR(15) ASCII NOT NULL ENABLE,
     "c_acctbal"    DECIMAL(12, 2) NOT NULL ENABLE,
     "c_mktsegment" CHAR(10) ASCII NOT NULL ENABLE,
     "c_comment"    VARCHAR(117) ASCII NOT NULL ENABLE
);

CREATE TABLE "exa_sf10_orders"
(
     "o_orderkey"      DECIMAL(18, 0) NOT NULL ENABLE,
     "o_custkey"       DECIMAL(18, 0) NOT NULL ENABLE,
     "o_orderstatus"   CHAR(1) ASCII NOT NULL ENABLE,
     "o_totalprice"    DECIMAL(15, 2) NOT NULL ENABLE,
     "o_orderdate"     DATE           NOT NULL ENABLE,
     "o_orderpriority" CHAR(15) ASCII NOT NULL ENABLE,
     "o_clerk"         CHAR(15) ASCII NOT NULL ENABLE,
     "o_shippriority"  DECIMAL(18, 0) NOT NULL ENABLE,
     "o_comment"       VARCHAR(79) ASCII NOT NULL ENABLE
);

CREATE TABLE "exa_sf10_lineitem"
(
     "l_orderkey"      DECIMAL(18, 0) NOT NULL ENABLE,
     "l_partkey"       DECIMAL(18, 0) NOT NULL ENABLE,
     "l_suppkey"       DECIMAL(18, 0) NOT NULL ENABLE,
     "l_linenumber"    DECIMAL(18, 0) NOT NULL ENABLE,
     "l_quantity"      DECIMAL(12, 2) NOT NULL ENABLE,
     "l_extendedprice" DECIMAL(12, 2) NOT NULL ENABLE,
     "l_discount"      DECIMAL(15, 2) NOT NULL ENABLE,
     "l_tax"           DECIMAL(15, 2) NOT NULL ENABLE,
     "l_returnflag"    CHAR(1) ASCII NOT NULL ENABLE,
     "l_linestatus"    CHAR(1) ASCII NOT NULL ENABLE,
     "l_shipdate"      DATE           NOT NULL ENABLE,
     "l_commitdate"    DATE           NOT NULL ENABLE,
     "l_receiptdate"   DATE           NOT NULL ENABLE,
     "l_shipinstruct"  CHAR(25) ASCII NOT NULL ENABLE,
     "l_shipmode"      CHAR(10) ASCII NOT NULL ENABLE,
     "l_comment"       VARCHAR(44) ASCII NOT NULL ENABLE
);