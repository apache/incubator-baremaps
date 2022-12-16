DROP MATERIALIZED VIEW IF EXISTS osm_highway CASCADE;

CREATE MATERIALIZED VIEW osm_highway AS
SELECT id, tags, geom
FROM (
    SELECT
        min(id) as id,
        jsonb_build_object('highway', tags -> 'highway', 'ref', tags -> 'ref') as tags,
        (st_dump(st_linemerge(st_collect(geom)))).geom as geom
    FROM osm_linestring
    WHERE tags ->> 'highway' IN ('motorway', 'motorway_link', 'trunk', 'trunk_link', 'primary', 'primary_link', 'secondary', 'secondary_link', 'tertiary',  'tertiary_link', 'unclassified', 'residential')
    AND tags ? 'ref'
    GROUP BY tags -> 'highway', tags -> 'ref'
    UNION ALL
    SELECT
        id,
        jsonb_build_object('highway', tags -> 'highway') as tags,
        geom
    FROM osm_linestring
    WHERE tags ->> 'highway' IN ('motorway', 'motorway_link', 'trunk', 'trunk_link', 'primary', 'primary_link', 'secondary', 'secondary_link', 'tertiary',  'tertiary_link', 'unclassified', 'residential')
    AND NOT (tags ? 'ref')
) AS merge;

CREATE INDEX IF NOT EXISTS osm_highway_geom_index ON osm_highway USING SPGIST (geom);
