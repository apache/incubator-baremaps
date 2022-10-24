DROP Materialized View IF EXISTS osm_landuse_relations;
CREATE MATERIALIZED VIEW osm_landuse_relations AS Select (ST_Dump(ST_Union(ST_MakeValid(osm_relations.geom)))).path[1] as id, '{"type": "multipolygon", "landuse": "forest"}'::jsonb as tags, (ST_Dump(ST_Union(ST_MakeValid(osm_relations.geom)))).geom as geom FROM osm_relations WHERE tags ? 'landuse' AND tags ->> 'type' = 'multipolygon';

DROP Materialized View IF EXISTS osm_landuse_ways;
CREATE MATERIALIZED VIEW osm_landuse_ways AS Select (ST_Dump(ST_Union(ST_MakeValid(osm_ways.geom)))).path[1] as id, '{"landuse": "forest"}'::jsonb as tags, (ST_Dump(ST_Union(ST_MakeValid(osm_ways.geom)))).geom as geom FROM osm_ways WHERE tags ? 'landuse';

DROP Materialized View IF EXISTS osm_landuse_relations_simplified;
CREATE MATERIALIZED VIEW osm_landuse_relations_simplified AS Select (ST_Dump(st_simplifypreservetopology(ST_Union(ST_MakeValid(geom)), 39135 / power(2, 7)))).path[1] as id, '{"type": "multipolygon", "landuse": "forest"}'::jsonb as tags, (ST_Dump(st_simplifypreservetopology(ST_Union(ST_MakeValid(geom)), 39135 / power(2, 7)))).geom as geom FROM osm_relations WHERE tags ? 'landuse' AND tags ->> 'type' = 'multipolygon';

DROP Materialized View IF EXISTS osm_landuse_ways_simplified;
CREATE MATERIALIZED VIEW osm_landuse_ways_simplified AS Select (ST_Dump(st_simplifypreservetopology(ST_Union(ST_MakeValid(geom)), 39135 / power(2, 7)))).path[1] as id, '{"type": "multipolygon", "landuse": "forest"}'::jsonb as tags, (ST_Dump(st_simplifypreservetopology(ST_Union(ST_MakeValid(geom)), 39135 / power(2, 7)))).geom as geom FROM osm_ways WHERE tags ? 'landuse' ;


DROP Materialized View IF EXISTS osm_landuse_relations_tags;
CREATE MATERIALIZED VIEW osm_landuse_relations_tags AS SELECT ROW_NUMBER() OVER (ORDER BY tags -> 'landuse') as id, jsonb_build_object('landuse', tags -> 'landuse') as tags,  ST_Union(ST_MakeValid(osm_relations.geom)) as geom FROM osm_relations WHERE tags ? 'landuse' AND tags ->> 'type' = 'multipolygon' Group By (tags -> 'landuse');


DROP Materialized View IF EXISTS osm_landuse_ways_tags;
CREATE MATERIALIZED VIEW osm_landuse_ways_tags AS SELECT ROW_NUMBER() OVER (ORDER BY tags -> 'landuse') as id, jsonb_build_object('landuse', tags -> 'landuse') as tags,  ST_Union(ST_MakeValid(osm_ways.geom)) as geom FROM osm_ways WHERE tags ? 'landuse' Group By (tags -> 'landuse');

DROP Materialized View IF EXISTS osm_landuse_relations_tags_2;
CREATE MATERIALIZED VIEW osm_landuse_relations_tags_2 AS SELECT (ST_Dump(geom)).path[1] as id , tags, st_simplifypreservetopology((ST_Dump(geom)).geom , 39135 / power(2, 7)) as geom FROM osm_landuse_relations_tags;

DROP Materialized View IF EXISTS osm_landuse_ways_tags_2;
CREATE MATERIALIZED VIEW osm_landuse_ways_tags_2 AS SELECT (ST_Dump(geom)).path[1] as id , tags, st_simplifypreservetopology((ST_Dump(geom)).geom, 39135 / power(2, 7)) as geom FROM osm_landuse_ways_tags;


-- DROP Materialized View IF EXISTS osm_landuse_relations_tags_3;
--CREATE MATERIALIZED VIEW osm_landuse_relations_tags_3 AS
--With test_polygons_1 as (
--Select tags, (ST_DUMP(geom)).geom as geom FROM osm_landuse_relations_tags WHERE ST_GeometryType(geom) = 'ST_MultiPolygon'
--Union ALL
--Select tags, ST_MakeValid(geom) FROM osm_landuse_relations_tags WHERE ST_GeometryType(geom) = 'ST_Polygon'
--UNION ALL
--SELECT tags ,ST_CollectionExtract(geom, 3) as geom  FROM  osm_landuse_relations_tags WHERE ST_GeometryType(geom) = 'ST_GeometryCollection')
--,
--test_polygons_2 as (
--Select tags, (ST_DUMP(geom)).geom as geom FROM test_polygons_1 WHERE ST_GeometryType(geom) = 'ST_MultiPolygon'
--Union ALL
--Select tags, ST_MakeValid(geom) FROM test_polygons_1 WHERE ST_GeometryType(geom) = 'ST_Polygon')
--Select ROW_NUMBER() OVER (ORDER BY (ST_DUMP(ST_union(geom))).geom) as id,  (array_agg(tags))[1] as tags, (ST_DUMP(ST_union(geom))).geom as geom from ( Select tags, ST_BuildArea((ST_DumpRings(geom)).geom) as geom from test_polygons_2) as data GROUP BY tags

DROP Materialized View IF EXISTS osm_landuse_relations_tags_4;
CREATE MATERIALIZED VIEW osm_landuse_relations_tags_4 AS
With test_polygons_1 as (
Select tags, (ST_DUMP(geom)).geom as geom FROM osm_landuse_relations_tags WHERE ST_GeometryType(geom) = 'ST_MultiPolygon'
Union ALL
Select tags, ST_MakeValid(geom) FROM osm_landuse_relations_tags WHERE ST_GeometryType(geom) = 'ST_Polygon'
UNION ALL
SELECT tags ,ST_CollectionExtract(geom, 3) as geom  FROM  osm_landuse_relations_tags WHERE ST_GeometryType(geom) = 'ST_GeometryCollection')
,
test_polygons_2 as (
Select tags, (ST_DUMP(geom)).geom as geom FROM test_polygons_1 WHERE ST_GeometryType(geom) = 'ST_MultiPolygon'
Union ALL
Select tags, ST_MakeValid(geom) FROM test_polygons_1 WHERE ST_GeometryType(geom) = 'ST_Polygon')
Select ROW_NUMBER() OVER (ORDER BY (ST_DUMP(ST_union(geom))).geom) as id,  (array_agg(tags))[1] as tags, (ST_DUMP(ST_union(geom))).geom as geom from ( Select tags, ST_BuildArea(ST_ExteriorRing(geom)) as geom from test_polygons_2) as data GROUP BY tags