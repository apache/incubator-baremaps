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
CREATE INDEX ip_start_index ON inetnum_locations (ip_start);
CREATE INDEX ip_end_index ON inetnum_locations (ip_end);