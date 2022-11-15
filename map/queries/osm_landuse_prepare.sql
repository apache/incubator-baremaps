DROP VIEW IF EXISTS osm_landuse CASCADE;

CREATE VIEW osm_landuse AS
SELECT id, tags, geom
FROM osm_polygon
WHERE geom IS NOT NULL AND tags ?| ARRAY ['landuse'];

CREATE MATERIALIZED VIEW osm_landuse_grouped AS
SELECT
    min(id) as id,
    jsonb_build_object('landuse', tags -> 'landuse') as tags,
    (st_dump(st_union(st_buildarea(st_exteriorring(geom))))).geom AS geom
FROM osm_landuse
WHERE tags ->> 'landuse' IN ('residential', 'farmland', 'forest', 'meadow', 'orchard', 'vineyard', 'salt_pond', 'water')
GROUP BY tags -> 'landuse';

CREATE INDEX osm_landuse_grouped_geom_idx ON osm_landuse_grouped USING GIST (geom);
