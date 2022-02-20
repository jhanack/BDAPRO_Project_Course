USE tpch;
LOAD DATA LOCAL INPATH '/data/sf10/nation.tbl' OVERWRITE INTO TABLE hive_sf10_nation;
LOAD DATA LOCAL INPATH '/data/sf10/region.tbl' OVERWRITE INTO TABLE hive_sf10_region;
LOAD DATA LOCAL INPATH '/data/sf10/part.tbl' OVERWRITE INTO TABLE hive_sf10_part;
LOAD DATA LOCAL INPATH '/data/sf10/supplier.tbl' OVERWRITE INTO TABLE hive_sf10_supplier;
LOAD DATA LOCAL INPATH '/data/sf10/partsupp.tbl' OVERWRITE INTO TABLE hive_sf10_partsupp;
LOAD DATA LOCAL INPATH '/data/sf10/customer.tbl' OVERWRITE INTO TABLE hive_sf10_customer;
LOAD DATA LOCAL INPATH '/data/sf10/orders.tbl' OVERWRITE INTO TABLE hive_sf10_orders;
LOAD DATA LOCAL INPATH '/data/sf10/lineitem.tbl' OVERWRITE INTO TABLE hive_sf10_lineitem;

