DROP VIEW IF EXISTS osm_landuse CASCADE;
DROP VIEW IF EXISTS osm_landuse_residential;
DROP VIEW IF EXISTS osm_landuse_farmland;
DROP VIEW IF EXISTS osm_landuse_forest;
DROP VIEW IF EXISTS osm_landuse_meadow;
DROP VIEW IF EXISTS osm_landuse_orchard;
DROP VIEW IF EXISTS osm_landuse_vineyard;
DROP VIEW IF EXISTS osm_landuse_salt_pond;
DROP VIEW IF EXISTS osm_landuse_water;

CREATE VIEW osm_landuse AS
SELECT
    id as id,
    jsonb_build_object('landuse', tags -> 'landuse') as tags,
    st_buildarea(st_exteriorring(geom)) as geom
FROM osm_polygon
WHERE geom IS NOT NULL AND tags ? 'landuse';

CREATE VIEW osm_landuse_residential AS
SELECT
    min(id) as id,
    jsonb_build_object('landuse', 'residential') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_landuse
         WHERE tags ->> 'landuse' = 'residential'
     ) osm_landuse
GROUP BY cluster;

CREATE VIEW osm_landuse_farmland AS
SELECT
    min(id) as id,
    jsonb_build_object('landuse', 'farmland') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_landuse
         WHERE tags ->> 'landuse' = 'farmland'
     ) osm_landuse
GROUP BY cluster;

CREATE VIEW osm_landuse_forest AS
SELECT
    min(id) as id,
    jsonb_build_object('landuse', 'forest') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_landuse
         WHERE tags ->> 'landuse' = 'forest'
     ) osm_landuse
GROUP BY cluster;

CREATE VIEW osm_landuse_meadow AS
SELECT
    min(id) as id,
    jsonb_build_object('landuse', 'meadow') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_landuse
         WHERE tags ->> 'landuse' = 'meadow'
     ) osm_landuse
GROUP BY cluster;

CREATE VIEW osm_landuse_orchard AS
SELECT
    min(id) as id,
    jsonb_build_object('landuse', 'orchard') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_landuse
         WHERE tags ->> 'landuse' = 'orchard'
     ) osm_landuse
GROUP BY cluster;

CREATE VIEW osm_landuse_vineyard AS
SELECT
    min(id) as id,
    jsonb_build_object('landuse', 'vineyard') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_landuse
         WHERE tags ->> 'landuse' = 'vineyard'
     ) osm_landuse
GROUP BY cluster;

CREATE VIEW osm_landuse_salt_pond AS
SELECT
    min(id) as id,
    jsonb_build_object('landuse', 'salt_pond') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_landuse
         WHERE tags ->> 'landuse' = 'salt_pond'
     ) osm_landuse
GROUP BY cluster;

CREATE VIEW osm_landuse_water AS
SELECT
    min(id) as id,
    jsonb_build_object('landuse', 'water') as tags,
    (st_dump(st_union(st_makevalid(geom)))).geom AS geom
FROM (
         SELECT
             id as id,
             geom as geom,
             st_clusterdbscan(geom, 0, 1) OVER() AS cluster
         FROM osm_landuse
         WHERE tags ->> 'landuse' = 'water'
     ) osm_landuse
GROUP BY cluster;

CREATE MATERIALIZED VIEW osm_landuse_grouped AS
SELECT id, tags, geom FROM osm_landuse_residential
UNION ALL
SELECT id, tags, geom FROM osm_landuse_farmland
UNION ALL
SELECT id, tags, geom FROM osm_landuse_forest
UNION ALL
SELECT id, tags, geom FROM osm_landuse_meadow
UNION ALL
SELECT id, tags, geom FROM osm_landuse_orchard
UNION ALL
SELECT id, tags, geom FROM osm_landuse_vineyard
UNION ALL
SELECT id, tags, geom FROM osm_landuse_salt_pond
UNION ALL
SELECT id, tags, geom FROM osm_landuse_water;

CREATE INDEX osm_landuse_grouped_geom_idx ON osm_landuse_grouped USING GIST (geom);

