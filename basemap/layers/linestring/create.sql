CREATE MATERIALIZED VIEW osm_linestring AS
SELECT id, tags, geom, changeset
FROM osm_ways LEFT JOIN osm_member ON id = member_ref
WHERE ST_GeometryType(osm_ways.geom) = 'ST_LineString'
  AND tags != '{}'
  AND member_ref IS NULL;