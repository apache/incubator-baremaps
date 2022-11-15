DROP MATERIALIZED VIEW IF EXISTS osm_railway CASCADE;

CREATE MATERIALIZED VIEW osm_railway AS
SELECT id, tags, geom
FROM (
   SELECT
       min(id) as id,
       jsonb_build_object('railway', tags -> 'railway') as tags,
       (st_dump(st_linemerge(st_collect(geom)))).geom as geom
   FROM osm_ways
   WHERE tags ->> 'railway' IN ('light_rail', 'monorail', 'rail', 'subway', 'tram')
   AND NOT tags ? 'service'
   GROUP BY tags -> 'railway'
) AS merge;

CREATE INDEX IF NOT EXISTS osm_railway_geom_index ON osm_railway USING SPGIST (geom);
