DROP VIEW IF EXISTS osm_natural CASCADE;

CREATE VIEW osm_natural AS
SELECT id, tags, geom
FROM osm_polygon
WHERE geom IS NOT NULL AND tags ?| ARRAY ['natural'];

CREATE MATERIALIZED VIEW osm_natural_grouped AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', tags -> 'natural') as tags,
    (st_dump(st_union(st_buildarea(st_exteriorring(geom))))).geom AS geom
FROM osm_natural
GROUP BY tags -> 'natural';

CREATE INDEX osm_natural_grouped_geom_idx ON osm_natural_grouped USING GIST (geom);
