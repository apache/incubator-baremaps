CREATE MATERIALIZED VIEW IF NOT EXISTS osm_polygon AS
SELECT id, tags, geom
FROM osm_way LEFT JOIN osm_member ON id = member_ref
WHERE ST_GeometryType( osm_way.geom) = 'ST_Polygon'
  AND tags != '{}'
  AND member_ref IS NULL
UNION
SELECT id, tags, geom
FROM osm_relation
WHERE ST_GeometryType( osm_relation.geom) = 'ST_Polygon'
  AND tags != '{}'
UNION
SELECT id, tags, (st_dump(geom)).geom as geom
FROM osm_relation
WHERE ST_GeometryType( osm_relation.geom) = 'ST_MultiPolygon'
  AND tags != '{}';