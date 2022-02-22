CREATE TABLE bdapro_schema(
   Hospital VARCHAR(22) NOT NULL
  ,Date     DATE  NOT NULL
  ,Bed_Type VARCHAR(20) NOT NULL
  ,Status   VARCHAR(32) NOT NULL
  ,Count    INTEGER  NOT NULL
  ,PRIMARY KEY(Date,Bed_Type, Status)
  );

\COPY bdapro_schema (Hospital, Date, Bed_Type, Status, Count) FROM '/var/lib/postgresql/csv_folder/COVID-19_Hospital_Capacity.csv' DELIMITER ',' CSV HEADER;