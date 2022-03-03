# INSTALL SONAME 'ha_connect';
#
# GRANT ALL PRIVILEGES ON *.* TO 'bdapro_user'@'%';
# FLUSH PRIVILEGES;

CREATE TABLE schema_a(
    uid INTEGER NOT NULL UNIQUE,
    name varchar(30) NOT NULL,
    age INTEGER NOT NULL,
    PRIMARY KEY (uid)
);

INSERT INTO schema_a VALUES (0, 'martha', 5);
INSERT INTO schema_a VALUES (1, 'bertha', 20);
INSERT INTO schema_a VALUES (2, 'moritz', 30);
INSERT INTO schema_a VALUES (3, 'timmothy', 25);
