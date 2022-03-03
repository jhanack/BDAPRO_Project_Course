/*
This class includes tests for one particular table distribution:
--------------------------------------------------
| pg-1   | lineitem, supplier                 |
| pg-2   | orders, customer		              |
| pg-3   | part, partsupp, nation, region     |
--------------------------------------------------
*/

select c.c_custkey,
       c.c_name,
       sum(l.l_extendedprice * (1 - l.l_discount)) as revenue,
       c.c_acctbal,
       n.n_name,
       c.c_address,
       c.c_phone,
       c.c_comment
from pg2.public.sf1_customer as c,
     pg2.public.sf1_orders as o,
     pg1.public.sf1_lineitem as l,
     pg3.public.sf1_nation as n
where c.c_custkey = o.o_custkey
    and l.l_orderkey = o.o_orderkey
    and o.o_orderdate >= date '1993-10-01'
    and o.o_orderdate < date '1994-01-01'
    and l.l_returnflag = 'R'
    and c.c_nationkey = n.n_nationkey
group by c.c_custkey,
         c.c_name,
         c.c_acctbal,
         c.c_phone,
         n.n_name,
         c.c_address,
         c.c_comment
order by revenue desc
limit 20;