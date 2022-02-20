IMPORT INTO "tpch"."exa_sf1_nation" from local CSV file '/data/sf1/nation.tbl' COLUMN SEPARATOR='|';
IMPORT INTO "tpch"."exa_sf1_region" from local CSV file '/data/sf1/region.tbl' COLUMN SEPARATOR='|';
IMPORT INTO "tpch"."exa_sf1_part" from local CSV file '/data/sf1/part.tbl' COLUMN SEPARATOR='|';
IMPORT INTO "tpch"."exa_sf1_supplier" from local CSV file '/data/sf1/supplier.tbl' COLUMN SEPARATOR='|';
IMPORT INTO "tpch"."exa_sf1_partsupp" from local CSV file '/data/sf1/partsupp.tbl' COLUMN SEPARATOR='|';
IMPORT INTO "tpch"."exa_sf1_customer" from local CSV file '/data/sf1/customer.tbl' COLUMN SEPARATOR='|';
IMPORT INTO "tpch"."exa_sf1_orders" from local CSV file '/data/sf1/orders.tbl' COLUMN SEPARATOR='|';
IMPORT INTO "tpch"."exa_sf1_lineitem" from local CSV file '/data/sf1/lineitem.tbl' COLUMN SEPARATOR='|';

IMPORT INTO "tpch"."exa_sf10_nation" from local CSV file '/data/sf10/nation.tbl' COLUMN SEPARATOR='|';
IMPORT INTO "tpch"."exa_sf10_region" from local CSV file '/data/sf10/region.tbl' COLUMN SEPARATOR='|';
IMPORT INTO "tpch"."exa_sf10_part" from local CSV file '/data/sf10/part.tbl' COLUMN SEPARATOR='|';
IMPORT INTO "tpch"."exa_sf10_supplier" from local CSV file '/data/sf10/supplier.tbl' COLUMN SEPARATOR='|';
IMPORT INTO "tpch"."exa_sf10_partsupp" from local CSV file '/data/sf10/partsupp.tbl' COLUMN SEPARATOR='|';
IMPORT INTO "tpch"."exa_sf10_customer" from local CSV file '/data/sf10/customer.tbl' COLUMN SEPARATOR='|';
IMPORT INTO "tpch"."exa_sf10_orders" from local CSV file '/data/sf10/orders.tbl' COLUMN SEPARATOR='|';
IMPORT INTO "tpch"."exa_sf10_lineitem" from local CSV file '/data/sf10/lineitem.tbl' COLUMN SEPARATOR='|';
