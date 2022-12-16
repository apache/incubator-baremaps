DROP MATERIALIZED VIEW IF EXISTS osm_boundary CASCADE;

CREATE MATERIALIZED VIEW osm_boundary AS
SELECT ROW_NUMBER() OVER () as id, tags, geom
FROM (
   SELECT jsonb_build_object('boundary', 'administrative', 'admin_level', tags -> 'admin_level', 'name', tags -> 'name') as tags, (st_dump(st_linemerge(st_collect(geom)))).geom as geom
   FROM osm_linestring
   WHERE tags ->> 'boundary' IN ('administrative')
     AND tags ->> 'admin_level' IN ('1', '2', '3', '4')
   GROUP BY tags -> 'admin_level', tags -> 'name'
) AS merge;

CREATE INDEX IF NOT EXISTS osm_boundary_geom_index ON osm_boundary USING SPGIST (geom);
