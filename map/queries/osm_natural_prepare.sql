DROP VIEW IF EXISTS osm_natural CASCADE;

CREATE MATERIALIZED VIEW osm_natural AS
SELECT
    id as id,
    jsonb_build_object('natural', tags -> 'natural') as tags,
    st_buildarea(st_exteriorring(geom)) as geom
FROM osm_polygon
WHERE geom IS NOT NULL
  AND tags ->> 'natural' IN ('grassland', 'heath', 'scrub', 'wood', 'bay', 'beach', 'glacier', 'mud', 'shingle', 'shoal', 'strait', 'water', 'wetland', 'bare_rock', 'sand', 'scree');

CREATE INDEX osm_natural_geom_idx ON osm_natural USING GIST (geom);

CREATE MATERIALIZED VIEW osm_natural_grouped AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', tags -> 'natural') as tags,
    (st_dump(st_buffer(st_collect(geom), 0))).geom AS geom
FROM osm_natural
GROUP BY tags -> 'natural';

CREATE INDEX osm_natural_grouped_geom_idx ON osm_natural_grouped USING GIST (geom);
