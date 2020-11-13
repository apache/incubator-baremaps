CREATE TABLE IF NOT EXISTS osm_headers
(
    replication_timestamp       timestamp without time zone NOT NULL,
    replication_sequence_number bigint                      NOT NULL,
    replication_url             text                        NOT NULL,
    source                      text,
    writing_program             text
);
CREATE TABLE osm_nodes
(
    id        bigint PRIMARY KEY,
    version   int                         NOT NULL,
    uid       int                         NOT NULL,
    timestamp timestamp without time zone NOT NULL,
    changeset bigint                      NOT NULL,
    tags      hstore,
    lon       float,
    lat       float,
    geom      geometry(point)
);
CREATE TABLE osm_ways
(
    id        bigint PRIMARY KEY,
    version   int                         NOT NULL,
    uid       int                         NOT NULL,
    timestamp timestamp without time zone NOT NULL,
    changeset bigint                      NOT NULL,
    tags      hstore,
    nodes     bigint[],
    geom      geometry
);
CREATE TABLE osm_relations
(
    id           bigint PRIMARY KEY,
    version      int                         NOT NULL,
    uid          int                         NOT NULL,
    timestamp    timestamp without time zone NOT NULL,
    changeset    bigint                      NOT NULL,
    tags         hstore,
    member_refs  bigint[],
    member_types int[],
    member_roles text[],
    geom         geometry
);