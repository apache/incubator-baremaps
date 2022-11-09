DROP INDEX IF EXISTS osm_relations_tags_index;
DROP INDEX IF EXISTS osm_relations_geom_index;

CREATE INDEX osm_relations_tags_index ON osm_relations USING gin (tags);
CREATE INDEX osm_relations_geom_index ON osm_relations USING spgist (geom);

DROP VIEW IF EXISTS osm_relations_z20 CASCADE;
DROP VIEW IF EXISTS osm_relations_z19 CASCADE;
DROP VIEW IF EXISTS osm_relations_z18 CASCADE;
DROP VIEW IF EXISTS osm_relations_z17 CASCADE;
DROP VIEW IF EXISTS osm_relations_z16 CASCADE;
DROP VIEW IF EXISTS osm_relations_z15 CASCADE;
DROP VIEW IF EXISTS osm_relations_z14 CASCADE;
DROP VIEW IF EXISTS osm_relations_z13 CASCADE;

DROP MATERIALIZED VIEW IF EXISTS osm_relations_z12 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_relations_z11 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_relations_z10 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_relations_z9 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_relations_z8 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_relations_z7 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_relations_z6 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_relations_z5 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_relations_z4 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_relations_z3 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_relations_z2 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_relations_z1 CASCADE;

CREATE VIEW osm_relations_z20 AS SELECT id, tags, geom FROM osm_relations;
CREATE VIEW osm_relations_z19 AS SELECT id, tags, geom FROM osm_relations;
CREATE VIEW osm_relations_z18 AS SELECT id, tags, geom FROM osm_relations;
CREATE VIEW osm_relations_z17 AS SELECT id, tags, geom FROM osm_relations;
CREATE VIEW osm_relations_z16 AS SELECT id, tags, geom FROM osm_relations;
CREATE VIEW osm_relations_z15 AS SELECT id, tags, geom FROM osm_relations;
CREATE VIEW osm_relations_z14 AS SELECT id, tags, geom FROM osm_relations;
CREATE VIEW osm_relations_z13 AS SELECT id, tags, geom FROM osm_relations;

CREATE MATERIALIZED VIEW osm_relations_z12 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 12)) AS geom
         FROM osm_relations
         WHERE tags ?| ARRAY ['landuse', 'natural', 'power', 'railway', 'route', 'waterway']
     ) AS osm_relations
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 12)), 2));

CREATE MATERIALIZED VIEW osm_relations_z11 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 11)) AS geom
         FROM osm_relations
         WHERE tags ?| ARRAY ['landuse', 'natural', 'waterway']
     ) AS osm_relations
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 11)), 2));

CREATE MATERIALIZED VIEW osm_relations_z10 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 10)) AS geom
         FROM osm_relations
         WHERE tags ?| ARRAY ['landuse', 'natural', 'waterway']
     ) AS osm_relations
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 10)), 2));

CREATE MATERIALIZED VIEW osm_relations_z9 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 9)) AS geom
         FROM osm_relations
         WHERE tags ?| ARRAY ['landuse', 'natural', 'waterway']
     ) AS osm_relations
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 9)), 2));

CREATE MATERIALIZED VIEW osm_relations_z8 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 8)) AS geom
         FROM osm_relations
         WHERE tags ?| ARRAY ['landuse', 'natural', 'waterway']
     ) AS osm_relations
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 8)), 2));

CREATE MATERIALIZED VIEW osm_relations_z7 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 7)) AS geom
         FROM osm_relations
         WHERE tags ?| ARRAY ['landuse', 'natural', 'waterway']
     ) AS osm_relations
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 7)), 2));

CREATE MATERIALIZED VIEW osm_relations_z6 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 6)) AS geom
         FROM osm_relations
         WHERE tags ?| ARRAY ['landuse', 'natural', 'waterway']
     ) AS osm_relations
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 6)), 2));

CREATE MATERIALIZED VIEW osm_relations_z5 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 5)) AS geom
         FROM osm_relations
         WHERE tags ?| ARRAY ['landuse', 'natural']
     ) AS osm_relations
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 5)), 2));

CREATE MATERIALIZED VIEW osm_relations_z4 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 4)) AS geom
         FROM osm_relations
         WHERE tags ?| ARRAY ['landuse', 'natural']
     ) AS osm_relations
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 4)), 2));

CREATE MATERIALIZED VIEW osm_relations_z3 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 3)) AS geom
         FROM osm_relations
         WHERE tags ?| ARRAY ['landuse', 'natural']
     ) AS osm_relations
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 3)), 2));

CREATE MATERIALIZED VIEW osm_relations_z2 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 2)) AS geom
         FROM osm_relations
         WHERE tags ?| ARRAY ['landuse', 'natural']
     ) AS osm_relations
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 2)), 2));

CREATE MATERIALIZED VIEW osm_relations_z1 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 1)) AS geom
         FROM osm_relations
         WHERE tags ?| ARRAY ['landuse', 'natural']
     ) AS osm_relations
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 1)), 2));

CREATE INDEX IF NOT EXISTS osm_relations_geom_index ON osm_relations USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_relations_geom_z12_index ON osm_relations_z12 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_relations_geom_z11_index ON osm_relations_z11 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_relations_geom_z10_index ON osm_relations_z10 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_relations_geom_z9_index ON osm_relations_z9 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_relations_geom_z8_index ON osm_relations_z8 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_relations_geom_z7_index ON osm_relations_z7 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_relations_geom_z6_index ON osm_relations_z6 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_relations_geom_z5_index ON osm_relations_z5 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_relations_geom_z4_index ON osm_relations_z4 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_relations_geom_z3_index ON osm_relations_z3 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_relations_geom_z2_index ON osm_relations_z2 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_relations_geom_z1_index ON osm_relations_z1 USING SPGIST (geom);
