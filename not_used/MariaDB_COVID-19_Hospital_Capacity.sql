CREATE TABLE bdapro_schema(
   Hospital VARCHAR(22) NOT NULL
  ,Date     DATE  NOT NULL
  ,Bed_Type VARCHAR(20) NOT NULL
  ,Status   VARCHAR(32) NOT NULL
  ,Count    INTEGER  NOT NULL
  ,PRIMARY KEY(Date,Bed_Type, Status)
  );

USE bdapro_database;
LOAD DATA LOCAL INFILE '/docker-entrypoint-initdb.d/COVID-19_Hospital_Capacity.csv' into TABLE bdapro_schema COLUMNS TERMINATED BY ',' IGNORE 1 LINES;