CREATE TABLE IF NOT EXISTS osm_way
(
    id        int8 PRIMARY KEY,
    version   int,
    uid       int,
    timestamp timestamp without time zone,
    changeset int8,
    tags      jsonb,
    nodes     int8[],
    geom      geometry
);
