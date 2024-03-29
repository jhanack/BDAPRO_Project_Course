/*
--------------------------------------------------
| pg-1   | lineitem, orders                   |
| pg-2   | customer, nation, region, supplier |
| pg-3   | part, partsupp                     |
--------------------------------------------------
 */

select l.l_orderkey,
    sum( l.l_extendedprice * (1 - l.l_discount )) as revenue,
    o.o_orderdate,
    o.o_shippriority
from sf10_customer as c,
    sf10_orders as o,
    sf10_lineitem as l
where c_custkey = o_custkey
    and l.l_orderkey = o.o_orderkey
    and c.c_mktsegment = 'BUILDING'
    and o.o_orderdate < date '1995-03-15'
    and l.l_shipdate > date '1995-03-15'
group by l_orderkey,
    o_orderdate,
    o_shippriority
order by revenue desc,
    o_orderdate
limit 10;