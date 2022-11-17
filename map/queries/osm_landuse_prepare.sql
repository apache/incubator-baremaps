DROP VIEW IF EXISTS osm_landuse CASCADE;

CREATE MATERIALIZED VIEW osm_landuse AS
SELECT
    id as id,
    jsonb_build_object('landuse', tags -> 'landuse') as tags,
    st_buildarea(st_exteriorring(geom)) as geom
FROM osm_polygon
WHERE geom IS NOT NULL
  AND tags ->> 'landuse' IN ('residential', 'farmland', 'forest', 'meadow', 'orchard', 'vineyard', 'salt_pond', 'water');

CREATE INDEX osm_landuse_geom_idx ON osm_landuse USING GIST (geom);

CREATE MATERIALIZED VIEW osm_landuse_grouped AS
SELECT
    min(id) as id,
    jsonb_build_object('landuse', tags -> 'landuse') as tags,
    (st_dump(st_buffer(st_collect(geom), 0))).geom AS geom
FROM osm_landuse
GROUP BY tags -> 'landuse';

CREATE INDEX osm_landuse_grouped_geom_idx ON osm_landuse_grouped USING GIST (geom);
