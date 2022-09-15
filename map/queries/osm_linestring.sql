DROP VIEW IF EXISTS osm_linestring CASCADE;

CREATE VIEW osm_linestring AS
SELECT id, tags, geom
FROM osm_ways
LEFT JOIN osm_way_member ON id = way_id
WHERE ST_GeometryType(osm_ways.geom) = 'ST_LineString'
  AND tags != '{}'
  AND way_id IS NULL;
