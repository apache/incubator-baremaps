DROP TABLE IF EXISTS inetnum_locations;
CREATE TABLE IF NOT EXISTS inetnum_locations (
    id integer PRIMARY KEY,
    name text NOT NULL,
    ip_start blob,
    ip_end blob,
    latitude real,
    longitude real
);