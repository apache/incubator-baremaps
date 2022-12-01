DROP TABLE IF EXISTS inetnum_locations;
CREATE TABLE IF NOT EXISTS inetnum_locations (
    id integer PRIMARY KEY,
    address text NOT NULL,
    ip_start blob,
    ip_end blob,
    latitude real,
    longitude real,
    network text,
    country text
);
CREATE INDEX inetnum_locations_ips ON inetnum_locations (ip_start,ip_end);