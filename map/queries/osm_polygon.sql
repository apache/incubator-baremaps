DROP VIEW IF EXISTS osm_polygon CASCADE;

CREATE VIEW osm_polygon AS
SELECT id, tags, geom
FROM osm_ways
LEFT JOIN osm_way_member ON id = way_id
WHERE ST_GeometryType(osm_ways.geom) = 'ST_Polygon'
  AND tags != '{}'
  AND way_id IS NULL;
