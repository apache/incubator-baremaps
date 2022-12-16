CREATE VIEW osm_natural_z20 AS
SELECT id, tags, geom FROM osm_natural;

CREATE VIEW osm_natural_z19 AS
SELECT id, tags, geom FROM osm_natural;

CREATE VIEW osm_natural_z18 AS
SELECT id, tags, geom FROM osm_natural;

CREATE VIEW osm_natural_z17 AS
SELECT id, tags, geom FROM osm_natural;

CREATE VIEW osm_natural_z16 AS
SELECT id, tags, geom FROM osm_natural;

CREATE VIEW osm_natural_z15 AS
SELECT id, tags, geom FROM osm_natural;

CREATE VIEW osm_natural_z14 AS
SELECT id, tags, geom FROM osm_natural;

CREATE VIEW osm_natural_z13 AS
SELECT id, tags, geom FROM osm_natural;

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z12 CASCADE;
CREATE MATERIALIZED VIEW osm_natural_z12 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 12)) AS geom
FROM osm_natural_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 12), 2);

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z11 CASCADE;
CREATE MATERIALIZED VIEW osm_natural_z11 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 11)) AS geom
FROM osm_natural_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 11), 2);

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z10 CASCADE;
CREATE MATERIALIZED VIEW osm_natural_z10 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 10)) AS geom
FROM osm_natural_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 10), 2);

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z9 CASCADE;
CREATE MATERIALIZED VIEW osm_natural_z9 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 9)) AS geom
FROM osm_natural_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 9), 2);

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z8 CASCADE;
CREATE MATERIALIZED VIEW osm_natural_z8 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 8)) AS geom
FROM osm_natural_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 8), 2);

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z7 CASCADE;
CREATE MATERIALIZED VIEW osm_natural_z7 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 7)) AS geom
FROM osm_natural_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 7), 2);

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z6 CASCADE;
CREATE MATERIALIZED VIEW osm_natural_z6 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 6)) AS geom
FROM osm_natural_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 6), 2);

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z5 CASCADE;
CREATE MATERIALIZED VIEW osm_natural_z5 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 5)) AS geom
FROM osm_natural_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 5), 2);

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z4 CASCADE;
CREATE MATERIALIZED VIEW osm_natural_z4 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 4)) AS geom
FROM osm_natural_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 4), 2);

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z3 CASCADE;
CREATE MATERIALIZED VIEW osm_natural_z3 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 3)) AS geom
FROM osm_natural_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 3), 2);

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z2 CASCADE;
CREATE MATERIALIZED VIEW osm_natural_z2 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 2)) AS geom
FROM osm_natural_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 2), 2);

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z1 CASCADE;
CREATE MATERIALIZED VIEW osm_natural_z1 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 1)) AS geom
FROM osm_natural_grouped
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 1), 2);
