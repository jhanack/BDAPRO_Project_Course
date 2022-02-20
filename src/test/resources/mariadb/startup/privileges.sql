-- GRANT ALL PRIVILEGES ON *.* TO 'mariadb'@'%';
--
-- FlUSH PRIVILEGES;

-- INSTALL SONAME 'ha_connect';
--
-- GRANT ALL PRIVILEGES ON *.* TO 'bdapro_user'@'%';
-- FLUSH PRIVILEGES;

USE bdapro_database;
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
-- CREATE TABLE schema_a(
--                          uid INTEGER NOT NULL UNIQUE,
--                          name varchar(30) NOT NULL,
--                          age INTEGER NOT NULL,
--                          PRIMARY KEY (uid)
-- );
--
-- INSERT INTO schema_a VALUES (0, 'martha', 5);
-- INSERT INTO schema_a VALUES (1, 'bertha', 20);
-- INSERT INTO schema_a VALUES (2, 'moritz', 30);
-- INSERT INTO schema_a VALUES (3, 'timmothy', 25);
