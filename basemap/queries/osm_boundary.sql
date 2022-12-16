DROP MATERIALIZED VIEW IF EXISTS osm_boundary CASCADE;

CREATE MATERIALIZED VIEW osm_boundary AS
SELECT ROW_NUMBER() OVER () as id, tags, geom
FROM (
   SELECT jsonb_build_object('boundary', 'administrative', 'admin_level', tags -> 'admin_level', 'name', tags -> 'name') as tags, (st_dump(st_linemerge(st_collect(geom)))).geom as geom
   FROM osm_linestring
   WHERE tags ->> 'boundary' IN ('administrative')
     AND tags ->> 'admin_level' IN ('1', '2', '3', '4')
   GROUP BY tags -> 'admin_level', tags -> 'name'
) AS merge;

CREATE MATERIALIZED VIEW osm_boundary_z1 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 1)) AS geom FROM osm_boundary) AS osm_boundary
WHERE geom IS NOT NULL AND (st_area(st_envelope(geom)) > power((78270 / power(2, 1)), 2));

CREATE MATERIALIZED VIEW osm_boundary_z2 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 2)) AS geom FROM osm_boundary) AS osm_boundary
WHERE geom IS NOT NULL AND (st_area(st_envelope(geom)) > power((78270 / power(2, 2)), 2));

CREATE MATERIALIZED VIEW osm_boundary_z3 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 3)) AS geom FROM osm_boundary) AS osm_boundary
WHERE geom IS NOT NULL AND (st_area(st_envelope(geom)) > power((78270 / power(2, 3)), 2));

CREATE MATERIALIZED VIEW osm_boundary_z4 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 4)) AS geom FROM osm_boundary) AS osm_boundary
WHERE geom IS NOT NULL AND (st_area(st_envelope(geom)) > power((78270 / power(2, 4)), 2));

CREATE MATERIALIZED VIEW osm_boundary_z5 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 5)) AS geom FROM osm_boundary) AS osm_boundary
WHERE geom IS NOT NULL AND (st_area(st_envelope(geom)) > power((78270 / power(2, 5)), 2));

CREATE MATERIALIZED VIEW osm_boundary_z6 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 6)) AS geom FROM osm_boundary) AS osm_boundary
WHERE geom IS NOT NULL AND (st_area(st_envelope(geom)) > power((78270 / power(2, 6)), 2));

CREATE MATERIALIZED VIEW osm_boundary_z7 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 7)) AS geom FROM osm_boundary) AS osm_boundary
WHERE geom IS NOT NULL AND (st_area(st_envelope(geom)) > power((78270 / power(2, 7)), 2));

CREATE MATERIALIZED VIEW osm_boundary_z8 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 8)) AS geom FROM osm_boundary) AS osm_boundary
WHERE geom IS NOT NULL AND (st_area(st_envelope(geom)) > power((78270 / power(2, 8)), 2));

CREATE MATERIALIZED VIEW osm_boundary_z9 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 9)) AS geom FROM osm_boundary) AS osm_boundary
WHERE geom IS NOT NULL AND (st_area(st_envelope(geom)) > power((78270 / power(2, 9)), 2));

CREATE MATERIALIZED VIEW osm_boundary_z10 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 10)) AS geom FROM osm_boundary) AS osm_boundary
WHERE geom IS NOT NULL AND (st_area(st_envelope(geom)) > power((78270 / power(2, 10)), 2));

CREATE MATERIALIZED VIEW osm_boundary_z11 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 11)) AS geom FROM osm_boundary) AS osm_boundary
WHERE geom IS NOT NULL AND (st_area(st_envelope(geom)) > power((78270 / power(2, 11)), 2));

CREATE MATERIALIZED VIEW osm_boundary_z12 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 12)) AS geom FROM osm_boundary) AS osm_boundary
WHERE geom IS NOT NULL AND (st_area(st_envelope(geom)) > power((78270 / power(2, 12)), 2));

CREATE VIEW osm_boundary_z13 AS SELECT id, tags, geom FROM osm_boundary;
CREATE VIEW osm_boundary_z14 AS SELECT id, tags, geom FROM osm_boundary;
CREATE VIEW osm_boundary_z15 AS SELECT id, tags, geom FROM osm_boundary;
CREATE VIEW osm_boundary_z16 AS SELECT id, tags, geom FROM osm_boundary;
CREATE VIEW osm_boundary_z17 AS SELECT id, tags, geom FROM osm_boundary;
CREATE VIEW osm_boundary_z18 AS SELECT id, tags, geom FROM osm_boundary;
CREATE VIEW osm_boundary_z19 AS SELECT id, tags, geom FROM osm_boundary;
CREATE VIEW osm_boundary_z20 AS SELECT id, tags, geom FROM osm_boundary;

CREATE INDEX IF NOT EXISTS osm_boundary_point_geom_index ON osm_boundary USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_boundary_point_geom_z1_index ON osm_boundary_z1 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_boundary_point_geom_z2_index ON osm_boundary_z2 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_boundary_point_geom_z3_index ON osm_boundary_z3 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_boundary_point_geom_z4_index ON osm_boundary_z4 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_boundary_point_geom_z5_index ON osm_boundary_z5 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_boundary_point_geom_z6_index ON osm_boundary_z6 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_boundary_point_geom_z7_index ON osm_boundary_z7 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_boundary_point_geom_z8_index ON osm_boundary_z8 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_boundary_point_geom_z9_index ON osm_boundary_z9 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_boundary_point_geom_z10_index ON osm_boundary_z10 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_boundary_point_geom_z11_index ON osm_boundary_z11 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_boundary_point_geom_z12_index ON osm_boundary_z12 USING SPGIST (geom);

