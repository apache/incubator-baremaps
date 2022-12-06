-- ('grassland', 'heath', 'scrub', 'wood', 'bay', 'beach', 'glacier', 'mud', 'shingle', 'shoal', 'strait', 'water', 'wetland', 'bare_rock', 'sand', 'scree');
DROP VIEW IF EXISTS osm_natural CASCADE;

DROP VIEW IF EXISTS osm_natural_mud;
DROP VIEW IF EXISTS osm_natural_grassland;
DROP VIEW IF EXISTS osm_natural_heath;
DROP VIEW IF EXISTS osm_natural_scrub;
DROP VIEW IF EXISTS osm_natural_wood;
DROP VIEW IF EXISTS osm_natural_bay;
DROP VIEW IF EXISTS osm_natural_beach;
DROP VIEW IF EXISTS osm_natural_glacier;
DROP VIEW IF EXISTS osm_natural_mud;
DROP VIEW IF EXISTS osm_natural_shingle;
DROP VIEW IF EXISTS osm_natural_shoal;
DROP VIEW IF EXISTS osm_natural_strait;
DROP VIEW IF EXISTS osm_natural_water;
DROP VIEW IF EXISTS osm_natural_wetland;
DROP VIEW IF EXISTS osm_natural_bare_rock;
DROP VIEW IF EXISTS osm_natural_sand;
DROP VIEW IF EXISTS osm_natural_scree;

CREATE VIEW osm_natural AS
SELECT
    id as id,
    jsonb_build_object('natural', tags -> 'natural') as tags,
    st_buildarea(st_exteriorring(geom)) as geom
FROM osm_polygon
WHERE geom IS NOT NULL AND tags ? 'natural';

CREATE VIEW osm_natural_grassland AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', 'grassland') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_natural
         WHERE tags ->> 'natural' = 'grassland'
     ) osm_natural
GROUP BY cluster;

CREATE VIEW osm_natural_heath AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', 'heath') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_natural
         WHERE tags ->> 'natural' = 'heath'
     ) osm_natural
GROUP BY cluster;

CREATE VIEW osm_natural_scrub AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', 'scrub') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_natural
         WHERE tags ->> 'natural' = 'scrub'
     ) osm_natural
GROUP BY cluster;

CREATE VIEW osm_natural_wood AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', 'wood') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_natural
         WHERE tags ->> 'natural' = 'wood'
     ) osm_natural
GROUP BY cluster;

CREATE VIEW osm_natural_bay AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', 'bay') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_natural
         WHERE tags ->> 'natural' = 'bay'
     ) osm_natural
GROUP BY cluster;

CREATE VIEW osm_natural_beach AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', 'beach') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_natural
         WHERE tags ->> 'natural' = 'beach'
     ) osm_natural
GROUP BY cluster;

CREATE VIEW osm_natural_glacier AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', 'glacier') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_natural
         WHERE tags ->> 'natural' = 'glacier'
     ) osm_natural
GROUP BY cluster;

CREATE VIEW osm_natural_mud AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', 'mud') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_natural
         WHERE tags ->> 'natural' = 'mud'
     ) osm_natural
GROUP BY cluster;

CREATE VIEW osm_natural_shingle AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', 'shingle') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_natural
         WHERE tags ->> 'natural' = 'shingle'
     ) osm_natural
GROUP BY cluster;

CREATE VIEW osm_natural_shoal AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', 'shoal') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_natural
         WHERE tags ->> 'natural' = 'shoal'
     ) osm_natural
GROUP BY cluster;

CREATE VIEW osm_natural_strait AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', 'strait') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_natural
         WHERE tags ->> 'natural' = 'strait'
     ) osm_natural
GROUP BY cluster;

CREATE VIEW osm_natural_water AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', 'water') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_natural
         WHERE tags ->> 'natural' = 'water'
     ) osm_natural
GROUP BY cluster;

CREATE VIEW osm_natural_wetland AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', 'wetland') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_natural
         WHERE tags ->> 'natural' = 'wetland'
     ) osm_natural
GROUP BY cluster;

CREATE VIEW osm_natural_bare_rock AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', 'bare_rock') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_natural
         WHERE tags ->> 'natural' = 'bare_rock'
     ) osm_natural
GROUP BY cluster;

CREATE VIEW osm_natural_sand AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', 'sand') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_natural
         WHERE tags ->> 'natural' = 'sand'
     ) osm_natural
GROUP BY cluster;

CREATE VIEW osm_natural_scree AS
SELECT
    min(id) as id,
    jsonb_build_object('natural', 'scree') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_natural
         WHERE tags ->> 'natural' = 'scree'
     ) osm_natural
GROUP BY cluster;

CREATE MATERIALIZED VIEW osm_natural_grouped AS
SELECT id, tags, geom FROM osm_natural_grassland
UNION ALL
SELECT id, tags, geom FROM osm_natural_heath
UNION ALL
SELECT id, tags, geom FROM osm_natural_scrub
UNION ALL
SELECT id, tags, geom FROM osm_natural_wood
UNION ALL
SELECT id, tags, geom FROM osm_natural_bay
UNION ALL
SELECT id, tags, geom FROM osm_natural_beach
UNION ALL
SELECT id, tags, geom FROM osm_natural_glacier
UNION ALL
SELECT id, tags, geom FROM osm_natural_mud
UNION ALL
SELECT id, tags, geom FROM osm_natural_shingle
UNION ALL
SELECT id, tags, geom FROM osm_natural_shoal
UNION ALL
SELECT id, tags, geom FROM osm_natural_strait
UNION ALL
SELECT id, tags, geom FROM osm_natural_water
UNION ALL
SELECT id, tags, geom FROM osm_natural_wetland
UNION ALL
SELECT id, tags, geom FROM osm_natural_bare_rock
UNION ALL
SELECT id, tags, geom FROM osm_natural_sand
UNION ALL
SELECT id, tags, geom FROM osm_natural_scree;


