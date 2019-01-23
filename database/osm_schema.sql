CREATE DATABASE osm;
CREATE USER osm WITH encrypted password 'osm';
grant all privileges on database osm to osm;

CREATE EXTENSION postgis;
CREATE EXTENSION hstore;

DROP TABLE IF EXISTS osm_nodes;
DROP TABLE IF EXISTS osm_ways;
DROP TABLE IF EXISTS osm_relations;

CREATE TABLE osm_nodes (
    id bigint NOT NULL,
    version int NOT NULL,
    uid int NOT NULL,
    timestamp timestamp without time zone NOT NULL,
    changeset bigint NOT NULL,
    tags hstore,
    geom geometry(point)
);

CREATE TABLE osm_ways (
    id bigint NOT NULL,
    version int NOT NULL,
    uid int NOT NULL,
    timestamp timestamp without time zone NOT NULL,
    changeset bigint NOT NULL,
    tags hstore,
    nodes bigint[],
    geom geometry
);

CREATE TABLE osm_relations (
    id bigint NOT NULL,
    version int NOT NULL,
    uid int NOT NULL,
    timestamp timestamp without time zone NOT NULL,
    changeset bigint NOT NULL,
    tags hstore,
    members bigint[],
    geom geometry
);

CREATE INDEX osm_nodes_gix ON osm_nodes USING GIST (geom);
CREATE INDEX osm_ways_gix ON osm_ways USING GIST (geom);
CREATE INDEX osm_relations_gix ON osm_relations USING GIST (geom);
