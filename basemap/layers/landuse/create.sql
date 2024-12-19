


CREATE MATERIALIZED VIEW osm_landuse_filtered AS
SELECT
    tags -> 'landuse' AS landuse,
    st_simplifypreservetopology(geom, 78270 / power(2, 12)) AS geom
FROM osm_polygon
WHERE geom IS NOT NULL
  AND st_area(geom) > 78270 / power(2, 12) * 100
  AND tags ->> 'landuse' IN ('commercial', 'construction', 'industrial', 'residential', 'retail', 'farmland', 'forest', 'meadow', 'greenhouse_horticulture', 'meadow', 'orchard', 'plant_nursery','vineyard', 'basin', 'salt_pond', 'brownfield', 'cemetery', 'grass', 'greenfield', 'landfill', 'military', 'quarry', 'railway');

CREATE MATERIALIZED VIEW osm_landuse_clustered AS
SELECT
    landuse,
    geom,
    st_clusterdbscan(geom, 0, 0) OVER(PARTITION BY landuse) AS cluster
FROM osm_landuse_filtered
WHERE geom IS NOT NULL;

CREATE MATERIALIZED VIEW osm_landuse_grouped AS
SELECT
    landuse,
    st_collect(geom) AS geom
FROM osm_landuse_clustered
GROUP BY landuse, cluster;

CREATE MATERIALIZED VIEW osm_landuse_buffered AS
SELECT
    landuse,
    st_buffer(geom, 0, 'join=mitre') AS geom
FROM osm_landuse_grouped;

CREATE MATERIALIZED VIEW osm_landuse_exploded AS
SELECT
    landuse,
    (st_dump(geom)).geom AS geom
FROM osm_landuse_buffered;

CREATE MATERIALIZED VIEW osm_landuse AS
SELECT
            row_number() OVER () AS id,
            jsonb_build_object('landuse', landuse) AS tags,
            geom
FROM osm_landuse_exploded;