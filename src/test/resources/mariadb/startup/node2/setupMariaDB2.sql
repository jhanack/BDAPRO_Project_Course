GRANT ALL PRIVILEGES ON *.* TO 'bdapro_user'@'%';
FLUSH PRIVILEGES;

CREATE TABLE schema_b(
    name varchar(30) NOT NULL,
    sales NUMERIC NOT NULL,
    region varchar(30) NOT NULL,
    PRIMARY KEY (name, region)
);

INSERT INTO schema_b VALUES ('martha', 100, 'america');
INSERT INTO schema_b VALUES ('bertha', 1200, 'europe');
INSERT INTO schema_b VALUES ('moritz', 850.3, 'europe');
INSERT INTO schema_b VALUES ('timmothy', 2001, 'asia');
