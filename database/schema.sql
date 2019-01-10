CREATE DATABASE osm;
CREATE USER osm WITH encrypted password 'osm';
grant all privileges on database osm to osm;

CREATE EXTENSION postgis;
CREATE EXTENSION hstore;

CREATE TABLE nodes (
    id bigint NOT NULL,
    version int NOT NULL,
    user_id int NOT NULL,
    tstamp timestamp without time zone NOT NULL,
    changeset_id bigint NOT NULL,
    tags hstore,
    geom geometry(point)
);

CREATE TABLE ways (
    id bigint NOT NULL,
    version int NOT NULL,
    user_id int NOT NULL,
    tstamp timestamp without time zone NOT NULL,
    changeset_id bigint NOT NULL,
    tags hstore,
    nodes bigint[],
    geom geometry(linestring)
);
