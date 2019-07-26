SELECT key,
       val,
       replace(st_geometrytype(geom), 'ST_', '')
FROM (
         SELECT DISTINCT 'aerialway' as key, val, geom
         FROM (
                  SELECT tags -> 'aerialway' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'aerialway'
                  UNION
                  SELECT tags -> 'aerialway' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'aerialway'
                  UNION
                  SELECT tags -> 'aerialway' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'aerialway'
              ) as aerialway
         UNION
         DISTINCT
         SELECT DISTINCT 'aeroway' as key, val, geom
         FROM (
                  SELECT tags -> 'aeroway' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'aeroway'
                  UNION
                  SELECT tags -> 'aeroway' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'aeroway'
                  UNION
                  SELECT tags -> 'aeroway' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'aeroway'
              ) as aeroway
         UNION
         DISTINCT
         SELECT DISTINCT 'amenity' as key, val, geom
         FROM (
                  SELECT tags -> 'amenity' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'amenity'
                  UNION
                  SELECT tags -> 'amenity' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'amenity'
                  UNION
                  SELECT tags -> 'amenity' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'amenity'
              ) as amenity
         UNION
         DISTINCT
         SELECT DISTINCT 'barrier' as key, val, geom
         FROM (
                  SELECT tags -> 'barrier' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'barrier'
                  UNION
                  SELECT tags -> 'barrier' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'barrier'
                  UNION
                  SELECT tags -> 'barrier' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'barrier'
              ) as barrier
         UNION
         DISTINCT
         SELECT DISTINCT 'boundary' as key, val, geom
         FROM (
                  SELECT tags -> 'boundary' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'boundary'
                  UNION
                  SELECT tags -> 'boundary' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'boundary'
                  UNION
                  SELECT tags -> 'boundary' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'boundary'
              ) as boundary
         UNION
         DISTINCT
         SELECT DISTINCT 'building' as key, val, geom
         FROM (
                  SELECT tags -> 'building' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'building'
                  UNION
                  SELECT tags -> 'building' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'building'
                  UNION
                  SELECT tags -> 'building' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'building'
              ) as building
         UNION
         DISTINCT
         SELECT DISTINCT 'craft' as key, val, geom
         FROM (
                  SELECT tags -> 'craft' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'craft'
                  UNION
                  SELECT tags -> 'craft' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'craft'
                  UNION
                  SELECT tags -> 'craft' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'craft'
              ) as craft
         UNION
         DISTINCT
         SELECT DISTINCT 'emergency' as key, val, geom
         FROM (
                  SELECT tags -> 'emergency' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'emergency'
                  UNION
                  SELECT tags -> 'emergency' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'emergency'
                  UNION
                  SELECT tags -> 'emergency' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'emergency'
              ) as emergency
         UNION
         DISTINCT
         SELECT DISTINCT 'geological' as key, val, geom
         FROM (
                  SELECT tags -> 'geological' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'geological'
                  UNION
                  SELECT tags -> 'geological' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'geological'
                  UNION
                  SELECT tags -> 'geological' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'geological'
              ) as geological
         UNION
         DISTINCT
         SELECT DISTINCT 'highway' as key, val, geom
         FROM (
                  SELECT tags -> 'highway' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'highway'
                  UNION
                  SELECT tags -> 'highway' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'highway'
                  UNION
                  SELECT tags -> 'highway' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'highway'
              ) as highway
         UNION
         DISTINCT
         SELECT DISTINCT 'cycleway' as key, val, geom
         FROM (
                  SELECT tags -> 'cycleway' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'cycleway'
                  UNION
                  SELECT tags -> 'cycleway' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'cycleway'
                  UNION
                  SELECT tags -> 'cycleway' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'cycleway'
              ) as cycleway
         UNION
         DISTINCT
         SELECT DISTINCT 'historic' as key, val, geom
         FROM (
                  SELECT tags -> 'historic' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'historic'
                  UNION
                  SELECT tags -> 'historic' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'historic'
                  UNION
                  SELECT tags -> 'historic' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'historic'
              ) as historic
         UNION
         DISTINCT
         SELECT DISTINCT 'landuse' as key, val, geom
         FROM (
                  SELECT tags -> 'landuse' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'landuse'
                  UNION
                  SELECT tags -> 'landuse' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'landuse'
                  UNION
                  SELECT tags -> 'landuse' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'landuse'
              ) as landuse
         UNION
         DISTINCT
         SELECT DISTINCT 'leisure' as key, val, geom
         FROM (
                  SELECT tags -> 'leisure' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'leisure'
                  UNION
                  SELECT tags -> 'leisure' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'leisure'
                  UNION
                  SELECT tags -> 'leisure' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'leisure'
              ) as leisure
         UNION
         DISTINCT
         SELECT DISTINCT 'man_made' as key, val, geom
         FROM (
                  SELECT tags -> 'man_made' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'man_made'
                  UNION
                  SELECT tags -> 'man_made' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'man_made'
                  UNION
                  SELECT tags -> 'man_made' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'man_made'
              ) as man_made
         UNION
         DISTINCT
         SELECT DISTINCT 'military' as key, val, geom
         FROM (
                  SELECT tags -> 'military' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'military'
                  UNION
                  SELECT tags -> 'military' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'military'
                  UNION
                  SELECT tags -> 'military' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'military'
              ) as military
         UNION
         DISTINCT
         SELECT DISTINCT 'natural' as key, val, geom
         FROM (
                  SELECT tags -> 'natural' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'natural'
                  UNION
                  SELECT tags -> 'natural' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'natural'
                  UNION
                  SELECT tags -> 'natural' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'natural'
              ) as land
         UNION
         DISTINCT
         SELECT DISTINCT 'office' as key, val, geom
         FROM (
                  SELECT tags -> 'office' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'office'
                  UNION
                  SELECT tags -> 'office' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'office'
                  UNION
                  SELECT tags -> 'office' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'office'
              ) as office
         UNION
         DISTINCT
         SELECT DISTINCT 'place' as key, val, geom
         FROM (
                  SELECT tags -> 'place' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'place'
                  UNION
                  SELECT tags -> 'place' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'place'
                  UNION
                  SELECT tags -> 'place' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'place'
              ) as place
         UNION
         DISTINCT
         SELECT DISTINCT 'power' as key, val, geom
         FROM (
                  SELECT tags -> 'power' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'power'
                  UNION
                  SELECT tags -> 'power' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'power'
                  UNION
                  SELECT tags -> 'power' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'power'
              ) as power
         UNION
         DISTINCT
         SELECT DISTINCT 'railway' as key, val, geom
         FROM (
                  SELECT tags -> 'railway' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'railway'
                  UNION
                  SELECT tags -> 'railway' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'railway'
                  UNION
                  SELECT tags -> 'railway' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'railway'
              ) as railway
         UNION
         DISTINCT
         SELECT DISTINCT 'route' as key, val, geom
         FROM (
                  SELECT tags -> 'route' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'route'
                  UNION
                  SELECT tags -> 'route' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'route'
                  UNION
                  SELECT tags -> 'route' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'route'
              ) as route
         UNION
         DISTINCT
         SELECT DISTINCT 'shop' as key, val, geom
         FROM (
                  SELECT tags -> 'shop' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'shop'
                  UNION
                  SELECT tags -> 'shop' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'shop'
                  UNION
                  SELECT tags -> 'shop' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'shop'
              ) as shop
         UNION
         DISTINCT
         SELECT DISTINCT 'sport' as key, val, geom
         FROM (
                  SELECT tags -> 'sport' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'sport'
                  UNION
                  SELECT tags -> 'sport' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'sport'
                  UNION
                  SELECT tags -> 'sport' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'sport'
              ) as sport
         UNION
         DISTINCT
         SELECT DISTINCT 'telecom' as key, val, geom
         FROM (
                  SELECT tags -> 'telecom' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'telecom'
                  UNION
                  SELECT tags -> 'telecom' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'telecom'
                  UNION
                  SELECT tags -> 'telecom' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'telecom'
              ) as telecom
         UNION
         DISTINCT
         SELECT DISTINCT 'tourism' as key, val, geom
         FROM (
                  SELECT tags -> 'tourism' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'tourism'
                  UNION
                  SELECT tags -> 'tourism' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'tourism'
                  UNION
                  SELECT tags -> 'tourism' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'tourism'
              ) as tourism
         UNION
         DISTINCT
         SELECT DISTINCT 'waterway' as key, val, geom
         FROM (
                  SELECT tags -> 'waterway' as val, st_geometrytype(geom) as geom
                  FROM osm_ways
                  WHERE tags ? 'waterway'
                  UNION
                  SELECT tags -> 'waterway' as val, st_geometrytype(geom) as geom
                  FROM osm_relations
                  WHERE tags ? 'waterway'
                  UNION
                  SELECT tags -> 'waterway' as val, st_geometrytype(geom) as geom
                  FROM osm_nodes
                  WHERE tags ? 'waterway'
              ) as waterway
     ) AS types
WHERE geom IS NOT NULL
ORDER BY key ASC
