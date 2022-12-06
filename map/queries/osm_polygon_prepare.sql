DROP MATERIALIZED VIEW IF EXISTS osm_polygon CASCADE;

CREATE MATERIALIZED VIEW osm_polygon AS
SELECT id, tags, geom
FROM osm_ways LEFT JOIN osm_ways_member ON id = way_id
WHERE ST_GeometryType(osm_ways.geom) = 'ST_Polygon'
  AND tags != '{}'
  AND way_id IS NULL
UNION
SELECT id, tags, geom
FROM osm_relations
WHERE ST_GeometryType(osm_relations.geom) = 'ST_Polygon'
  AND tags != '{}'
UNION
SELECT id, tags, (st_dump(geom)).geom as geom
FROM osm_relations
WHERE ST_GeometryType(osm_relations.geom) = 'ST_MultiPolygon'
  AND tags != '{}';
