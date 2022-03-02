#!/bin/bash

shopt -s globstar

cd /tpch-dbgen

for sf in 1 10; do
  echo "Generating data for sf"${sf}
  ./dbgen -vf -s ${sf}
  mkdir -p /data/sf${sf}
  mv *.tbl /data/sf${sf}/
done

##20 30 40 50