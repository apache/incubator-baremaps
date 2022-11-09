DROP VIEW IF EXISTS osm_landuse CASCADE;

CREATE VIEW osm_landuse AS
SELECT id, tags, geom
FROM osm_polygon
WHERE geom IS NOT NULL AND tags ?| ARRAY ['landuse'];

CREATE VIEW osm_landuse_z20 AS SELECT id, tags, geom FROM osm_landuse;
CREATE VIEW osm_landuse_z19 AS SELECT id, tags, geom FROM osm_landuse;
CREATE VIEW osm_landuse_z18 AS SELECT id, tags, geom FROM osm_landuse;
CREATE VIEW osm_landuse_z17 AS SELECT id, tags, geom FROM osm_landuse;
CREATE VIEW osm_landuse_z16 AS SELECT id, tags, geom FROM osm_landuse;
CREATE VIEW osm_landuse_z15 AS SELECT id, tags, geom FROM osm_landuse;
CREATE VIEW osm_landuse_z14 AS SELECT id, tags, geom FROM osm_landuse;
CREATE VIEW osm_landuse_z13 AS SELECT id, tags, geom FROM osm_landuse;

CREATE MATERIALIZED VIEW osm_landuse_grouped AS
SELECT
    min(id) as id,
    jsonb_build_object('landuse', tags -> 'landuse') as tags,
    (st_dump(st_union(st_buildarea(st_exteriorring(geom))))).geom AS geom
FROM osm_landuse
WHERE tags ->> 'landuse' IN ('residential', 'farmland', 'forest', 'meadow', 'orchard', 'vineyard', 'salt_pond', 'water')
GROUP BY tags -> 'landuse';
CREATE INDEX osm_landuse_grouped_geom_idx ON osm_landuse_grouped USING GIST (geom);

CREATE MATERIALIZED VIEW osm_landuse_z12 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 12)) AS geom
FROM osm_landuse_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 12), 2);
CREATE INDEX IF NOT EXISTS osm_landuse_geom_z12_index ON osm_landuse_z12 USING SPGIST (geom);

CREATE MATERIALIZED VIEW osm_landuse_z11 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 11)) AS geom
FROM osm_landuse_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 11), 2);
CREATE INDEX IF NOT EXISTS osm_landuse_geom_z11_index ON osm_landuse_z11 USING SPGIST (geom);

CREATE MATERIALIZED VIEW osm_landuse_z10 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 10)) AS geom
FROM osm_landuse_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 10), 2);
CREATE INDEX IF NOT EXISTS osm_landuse_geom_z10_index ON osm_landuse_z10 USING SPGIST (geom);

CREATE MATERIALIZED VIEW osm_landuse_z9 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 9)) AS geom
FROM osm_landuse_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 9), 2);
CREATE INDEX IF NOT EXISTS osm_landuse_geom_z9_index ON osm_landuse_z9 USING SPGIST (geom);

CREATE MATERIALIZED VIEW osm_landuse_z8 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 8)) AS geom
FROM osm_landuse_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 8), 2);
CREATE INDEX IF NOT EXISTS osm_landuse_geom_z8_index ON osm_landuse_z8 USING SPGIST (geom);

CREATE MATERIALIZED VIEW osm_landuse_z7 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 7)) AS geom
FROM osm_landuse_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 7), 2);
CREATE INDEX IF NOT EXISTS osm_landuse_geom_z7_index ON osm_landuse_z7 USING SPGIST (geom);

CREATE MATERIALIZED VIEW osm_landuse_z6 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 6)) AS geom
FROM osm_landuse_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 6), 2);
CREATE INDEX IF NOT EXISTS osm_landuse_geom_z6_index ON osm_landuse_z6 USING SPGIST (geom);

CREATE MATERIALIZED VIEW osm_landuse_z5 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 5)) AS geom
FROM osm_landuse_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 5), 2);
CREATE INDEX IF NOT EXISTS osm_landuse_geom_z5_index ON osm_landuse_z5 USING SPGIST (geom);

CREATE MATERIALIZED VIEW osm_landuse_z4 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 4)) AS geom
FROM osm_landuse_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 4), 2);
CREATE INDEX IF NOT EXISTS osm_landuse_geom_z4_index ON osm_landuse_z4 USING SPGIST (geom);

CREATE MATERIALIZED VIEW osm_landuse_z3 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 3)) AS geom
FROM osm_landuse_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 3), 2);
CREATE INDEX IF NOT EXISTS osm_landuse_geom_z3_index ON osm_landuse_z3 USING SPGIST (geom);

CREATE MATERIALIZED VIEW osm_landuse_z2 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 2)) AS geom
FROM osm_landuse_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 2), 2);
CREATE INDEX IF NOT EXISTS osm_landuse_geom_z2_index ON osm_landuse_z2 USING SPGIST (geom);

CREATE MATERIALIZED VIEW osm_landuse_z1 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 1)) AS geom
FROM osm_landuse_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 1), 2);
CREATE INDEX IF NOT EXISTS osm_landuse_geom_z1_index ON osm_landuse_z1 USING SPGIST (geom);
