CREATE TABLE IF NOT EXISTS osm_headers
(
    replication_timestamp       bigint NOT NULL,
    replication_sequence_number bigint NOT NULL,
    replication_url             text   NOT NULL,
    source                      text,
    writing_program             text,
    bbox                        geometry
);
CREATE TABLE osm_nodes
(
    id        bigint                      NOT NULL,
    version   int                         NOT NULL,
    uid       int                         NOT NULL,
    timestamp timestamp without time zone NOT NULL,
    changeset bigint                      NOT NULL,
    tags      hstore,
    geom      geometry(point)
);
CREATE TABLE osm_ways
(
    id        bigint                      NOT NULL,
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
    id           bigint                      NOT NULL,
    version      int                         NOT NULL,
    uid          int                         NOT NULL,
    timestamp    timestamp without time zone NOT NULL,
    changeset    bigint                      NOT NULL,
    tags         hstore,
    member_refs  bigint[],
    member_types text[],
    member_roles text[],
    geom         geometry
);