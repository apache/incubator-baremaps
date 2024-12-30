CREATE MATERIALIZED VIEW IF NOT EXISTS osm_member AS
SELECT DISTINCT member_ref as member_ref
FROM osm_relation,
     unnest(member_types, member_refs) AS way(member_type, member_ref)
WHERE geom IS NOT NULL
  AND member_type = 1
  AND tags ->> 'type' = 'multipolygon'
  AND NOT tags ->> 'natural' = 'coastline';