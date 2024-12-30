CREATE MATERIALIZED VIEW IF NOT EXISTS osm_linestring AS
SELECT id, tags, geom, changeset
FROM osm_way LEFT JOIN osm_member ON id = member_ref
WHERE ST_GeometryType( osm_way.geom) = 'ST_LineString'
  AND tags != '{}'
  AND member_ref IS NULL;