CREATE DATABASE osm;
CREATE USER osm WITH encrypted password 'osm';
grant all privileges on database osm to osm;

CREATE EXTENSION postgis;
CREATE EXTENSION hstore;

DROP TABLE IF EXISTS nodes;
DROP TABLE IF EXISTS ways;
DROP TABLE IF EXISTS relations;

CREATE TABLE nodes (
    id bigint NOT NULL,
    version int NOT NULL,
    uid int NOT NULL,
    timestamp timestamp without time zone NOT NULL,
    changeset bigint NOT NULL,
    tags hstore,
    geom geometry(point)
);

CREATE TABLE ways (
    id bigint NOT NULL,
    version int NOT NULL,
    uid int NOT NULL,
    timestamp timestamp without time zone NOT NULL,
    changeset bigint NOT NULL,
    tags hstore,
    nodes bigint[],
    geom geometry
);

CREATE TABLE relations (
    id bigint NOT NULL,
    version int NOT NULL,
    uid int NOT NULL,
    timestamp timestamp without time zone NOT NULL,
    changeset bigint NOT NULL,
    tags hstore,
    members bigint[],
    geom geometry
);
