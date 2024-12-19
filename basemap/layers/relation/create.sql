CREATE TABLE IF NOT EXISTS osm_relations
(
    id           int8 PRIMARY KEY,
    version      int,
    uid          int,
    timestamp    timestamp without time zone,
    changeset    int8,
    tags         jsonb,
    member_refs  bigint[],
    member_types int[],
    member_roles text[],
    geom         geometry
);