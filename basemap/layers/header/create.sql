CREATE TABLE IF NOT EXISTS osm_headers
(
    replication_sequence_number bigint PRIMARY KEY,
    replication_timestamp       timestamp without time zone,
    replication_url             text,
    source                      text,
    writing_program             text
);