CREATE TABLE IF NOT EXISTS osm_headers
(
    replication_timestamp       timestamp without time zone,
    replication_sequence_number bigint,
    replication_url             text,
    source                      text,
    writing_program             text
);
CREATE TABLE osm_nodes
(
    id        bigint PRIMARY KEY,
    version   int,
    uid       int,
    timestamp timestamp without time zone,
    changeset bigint,
    tags      hstore,
    lon       float,
    lat       float,
    geom      geometry(point)
);
CREATE TABLE osm_ways
(
    id        bigint PRIMARY KEY,
    version   int,
    uid       int,
    timestamp timestamp without time zone,
    changeset bigint,
    tags      hstore,
    nodes     bigint[],
    geom      geometry
);
CREATE TABLE osm_relations
(
    id           bigint PRIMARY KEY,
    version      int,
    uid          int,
    timestamp    timestamp without time zone,
    changeset    bigint,
    tags         hstore,
    member_refs  bigint[],
    member_types int[],
    member_roles text[],
    geom         geometry
);