CREATE TABLE IF NOT EXISTS osm_node
(
    id        int8 PRIMARY KEY,
    version   int,
    uid       int,
    timestamp timestamp without time zone,
    changeset int8,
    tags      jsonb,
    lon       float,
    lat       float,
    geom      geometry(point)
);