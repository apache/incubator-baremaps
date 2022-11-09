DROP MATERIALIZED VIEW IF EXISTS osm_ways_member CASCADE;

CREATE MATERIALIZED VIEW osm_ways_member AS
SELECT DISTINCT member_ref as way_id
FROM osm_relations, unnest(member_types, member_refs) AS way(member_type, member_ref)
WHERE geom IS NOT NULL
  AND member_type = 1
  AND tags ->> 'type' = 'multipolygon'
  AND NOT tags ->> 'natural' = 'coastline';

CREATE INDEX osm_ways_member_index ON osm_ways_member(way_id);
