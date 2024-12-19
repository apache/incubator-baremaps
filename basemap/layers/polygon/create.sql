CREATE MATERIALIZED VIEW osm_polygon AS
SELECT id, tags, geom
FROM osm_ways LEFT JOIN osm_member ON id = member_ref
WHERE ST_GeometryType(osm_ways.geom) = 'ST_Polygon'
  AND tags != '{}'
  AND member_ref IS NULL
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