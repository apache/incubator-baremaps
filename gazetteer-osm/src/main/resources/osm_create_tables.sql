DROP TABLE IF EXISTS osm_info;
CREATE TABLE IF NOT EXISTS osm_info (
    version integer NOT NULL
);

DROP TABLE IF EXISTS osm_users;
CREATE TABLE IF NOT EXISTS osm_users (
    id int NOT NULL,
    name text NOT NULL
);

DROP TABLE IF EXISTS osm_nodes;
CREATE TABLE osm_nodes (
    id bigint NOT NULL,
    version int NOT NULL,
    uid int NOT NULL,
    timestamp timestamp without time zone NOT NULL,
    changeset bigint NOT NULL,
    tags hstore,
    geom geometry(point)
);

DROP TABLE IF EXISTS osm_ways;
CREATE TABLE osm_ways (
    id bigint NOT NULL,
    version int NOT NULL,
    uid int NOT NULL,
    timestamp timestamp without time zone NOT NULL,
    changeset bigint NOT NULL,
    tags hstore,
    nodes bigint[]
);

DROP TABLE IF EXISTS osm_relations;
CREATE TABLE osm_relations (
    id bigint NOT NULL,
    version int NOT NULL,
    uid int NOT NULL,
    timestamp timestamp without time zone NOT NULL,
    changeset bigint NOT NULL,
    tags hstore,
    member_refs bigint[],
    member_types text[],
    member_roles text[]
);
