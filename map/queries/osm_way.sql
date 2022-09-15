DROP VIEW IF EXISTS osm_way CASCADE;

CREATE VIEW osm_way AS SELECT * FROM osm_ways;

DROP INDEX IF EXISTS osm_way_tags_index;
DROP INDEX IF EXISTS osm_way_geom_index;

CREATE INDEX osm_way_tags_index ON osm_ways USING gin (tags);
CREATE INDEX osm_way_geom_index ON osm_ways USING spgist (geom);

CREATE MATERIALIZED VIEW osm_way_z1 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 39135 / power(2, 1)) AS geom
         FROM osm_ways
         WHERE tags ?| ARRAY ['landuse', 'natural']
     ) AS osm_ways
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((39135 / power(2, 1)), 2));

CREATE MATERIALIZED VIEW osm_way_z2 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 39135 / power(2, 2)) AS geom
         FROM osm_ways
         WHERE tags ?| ARRAY ['landuse', 'natural']
     ) AS osm_ways
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((39135 / power(2, 2)), 2));

CREATE MATERIALIZED VIEW osm_way_z3 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 39135 / power(2, 3)) AS geom
         FROM osm_ways
         WHERE tags ?| ARRAY ['landuse', 'natural']
     ) AS osm_ways
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((39135 / power(2, 3)), 2));

CREATE MATERIALIZED VIEW osm_way_z4 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 39135 / power(2, 4)) AS geom
         FROM osm_ways
         WHERE tags ?| ARRAY ['landuse', 'natural']
     ) AS osm_ways
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((39135 / power(2, 4)), 2));

CREATE MATERIALIZED VIEW osm_way_z5 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 39135 / power(2, 5)) AS geom
         FROM osm_ways
         WHERE tags ?| ARRAY ['landuse', 'natural']
     ) AS osm_ways
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((39135 / power(2, 5)), 2));

CREATE MATERIALIZED VIEW osm_way_z6 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 39135 / power(2, 6)) AS geom
         FROM osm_ways
         WHERE tags ?| ARRAY ['landuse', 'natural', 'waterway' ]
     ) AS osm_ways
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((39135 / power(2, 6)), 2));

CREATE MATERIALIZED VIEW osm_way_z7 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 39135 / power(2, 7)) AS geom
         FROM osm_ways
         WHERE tags ?| ARRAY ['landuse', 'natural', 'waterway' ]
     ) AS osm_ways
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((39135 / power(2, 7)), 2));

CREATE MATERIALIZED VIEW osm_way_z8 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 39135 / power(2, 8)) AS geom
         FROM osm_ways
         WHERE tags ?| ARRAY ['landuse', 'natural', 'waterway' ]
     ) AS osm_ways
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((39135 / power(2, 8)), 2));

CREATE MATERIALIZED VIEW osm_way_z9 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 39135 / power(2, 9)) AS geom
         FROM osm_ways
         WHERE tags ?| ARRAY ['landuse', 'natural', 'waterway' ]
     ) AS osm_ways
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((39135 / power(2, 9)), 2));

CREATE MATERIALIZED VIEW osm_way_z10 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 39135 / power(2, 10)) AS geom
         FROM osm_ways
         WHERE tags ?| ARRAY ['landuse', 'natural', 'waterway' ]
     ) AS osm_ways
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((39135 / power(2, 10)), 2));

CREATE MATERIALIZED VIEW osm_way_z11 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 39135 / power(2, 11)) AS geom
         FROM osm_ways
         WHERE tags ?| ARRAY ['landuse', 'natural', 'waterway' ]
     ) AS osm_ways
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((39135 / power(2, 11)), 2));

CREATE MATERIALIZED VIEW osm_way_z12 AS
SELECT id, tags, geom
FROM (
         SELECT id, tags, st_simplifypreservetopology(geom, 39135 / power(2, 12)) AS geom
         FROM osm_ways
         WHERE tags ?| ARRAY [ 'landuse', 'natural', 'power', 'railway', 'route', 'waterway' ]
     ) AS osm_ways
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((39135 / power(2, 12)), 2));

CREATE VIEW osm_way_z13 AS SELECT id, tags, geom FROM osm_way;
CREATE VIEW osm_way_z14 AS SELECT id, tags, geom FROM osm_way;
CREATE VIEW osm_way_z15 AS SELECT id, tags, geom FROM osm_way;
CREATE VIEW osm_way_z16 AS SELECT id, tags, geom FROM osm_way;
CREATE VIEW osm_way_z17 AS SELECT id, tags, geom FROM osm_way;
CREATE VIEW osm_way_z18 AS SELECT id, tags, geom FROM osm_way;
CREATE VIEW osm_way_z19 AS SELECT id, tags, geom FROM osm_way;
CREATE VIEW osm_way_z20 AS SELECT id, tags, geom FROM osm_way;

CREATE INDEX IF NOT EXISTS osm_way_index ON osm_ways USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_way_z1_index ON osm_way_z1 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_way_z2_index ON osm_way_z2 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_way_z3_index ON osm_way_z3 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_way_z4_index ON osm_way_z4 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_way_z5_index ON osm_way_z5 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_way_z6_index ON osm_way_z6 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_way_z7_index ON osm_way_z7 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_way_z8_index ON osm_way_z8 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_way_z9_index ON osm_way_z9 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_way_z10_index ON osm_way_z10 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_way_z11_index ON osm_way_z11 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_way_z12_index ON osm_way_z12 USING SPGIST (geom);
