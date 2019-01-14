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

CREATE INDEX nodes_gix ON nodes USING GIST (geom);
CREATE INDEX ways_gix ON ways USING GIST (geom);
CREATE INDEX relations_gix ON relations USING GIST (geom);

CREATE OR REPLACE FUNCTION BBox(x integer, y integer, zoom integer)
    RETURNS geometry AS
$BODY$
DECLARE
    max numeric := 6378137 * pi();
    res numeric := max * 2 / 2^zoom;
    bbox geometry;
BEGIN
    return ST_MakeEnvelope(
        -max + (x * res),
        max - (y * res),
        -max + (x * res) + res,
        max - (y * res) - res,
        3857);
END;
$BODY$
LANGUAGE plpgsql IMMUTABLE;

SELECT ST_AsMVT(q, 'buildings', 4096, 'geom')
FROM (
  SELECT id, name, type,
    ST_AsMvtGeom(
      geometry,
      BBox(16597, 11273, 15),
      4096,
      256,
      true
    ) AS geom
  FROM import.osm_buildings
  WHERE geometry && BBox(16597, 11273, 15)
  AND ST_Intersects(geometry, BBox(16597, 11273, 15))
) AS q;


SELECT ST_AsMVT(q, 'layer', 4096, 'geom')
FROM (
  SELECT ST_AsMVTGeom(geom, ST_MakeEnvelope(6.679688, 46.528635, 6.701660, 46.543750, 4326), 4096, 256, true) geom
  FROM ways c
  WHERE geom && ST_MakeEnvelope(6.679688, 46.528635, 6.701660, 46.543750, 4326)
  AND tags -> 'building' = 'yes'
) q


