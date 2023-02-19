CREATE VIEW globaladm1_z20 AS
SELECT fid, shapegroup, shapetype, geom FROM globaladm1;

CREATE VIEW globaladm1_z19 AS
SELECT fid, shapegroup, shapetype, geom FROM globaladm1;

CREATE VIEW globaladm1_z18 AS
SELECT fid, shapegroup, shapetype, geom FROM globaladm1;

CREATE VIEW globaladm1_z17 AS
SELECT fid, shapegroup, shapetype, geom FROM globaladm1;

CREATE VIEW globaladm1_z16 AS
SELECT fid, shapegroup, shapetype, geom FROM globaladm1;

CREATE VIEW globaladm1_z15 AS
SELECT fid, shapegroup, shapetype, geom FROM globaladm1;

CREATE VIEW globaladm1_z14 AS
SELECT fid, shapegroup, shapetype, geom FROM globaladm1;

CREATE VIEW globaladm1_z13 AS
SELECT fid, shapegroup, shapetype, geom FROM globaladm1;

CREATE MATERIALIZED VIEW globaladm1_z12 AS
SELECT fid, shapegroup, shapetype, st_simplifypreservetopology(geom, 78270 / power(2, 12)) AS geom FROM globaladm1;

CREATE MATERIALIZED VIEW globaladm1_z11 AS
SELECT fid, shapegroup, shapetype, st_simplifypreservetopology(geom, 78270 / power(2, 11)) AS geom FROM globaladm1;

CREATE MATERIALIZED VIEW globaladm1_z10 AS
SELECT fid, shapegroup, shapetype, st_simplifypreservetopology(geom, 78270 / power(2, 10)) AS geom FROM globaladm1;

CREATE MATERIALIZED VIEW globaladm1_z9 AS
SELECT fid, shapegroup, shapetype, st_simplifypreservetopology(geom, 78270 / power(2, 9)) AS geom FROM globaladm1;

CREATE MATERIALIZED VIEW globaladm1_z8 AS
SELECT fid, shapegroup, shapetype, st_simplifypreservetopology(geom, 78270 / power(2, 8)) AS geom FROM globaladm1;

CREATE MATERIALIZED VIEW globaladm1_z7 AS
SELECT fid, shapegroup, shapetype, st_simplifypreservetopology(geom, 78270 / power(2, 7)) AS geom FROM globaladm1;

CREATE MATERIALIZED VIEW globaladm1_z6 AS
SELECT fid, shapegroup, shapetype, st_simplifypreservetopology(geom, 78270 / power(2, 6)) AS geom FROM globaladm1;

CREATE MATERIALIZED VIEW globaladm1_z5 AS
SELECT fid, shapegroup, shapetype, st_simplifypreservetopology(geom, 78270 / power(2, 5)) AS geom FROM globaladm1;

CREATE MATERIALIZED VIEW globaladm1_z4 AS
SELECT fid, shapegroup, shapetype, st_simplifypreservetopology(geom, 78270 / power(2, 4)) AS geom FROM globaladm1;

CREATE MATERIALIZED VIEW globaladm1_z3 AS
SELECT fid, shapegroup, shapetype, st_simplifypreservetopology(geom, 78270 / power(2, 3)) AS geom FROM globaladm1;

CREATE MATERIALIZED VIEW globaladm1_z2 AS
SELECT fid, shapegroup, shapetype, st_simplifypreservetopology(geom, 78270 / power(2, 2)) AS geom FROM globaladm1;

CREATE MATERIALIZED VIEW globaladm1_z1 AS
SELECT fid, shapegroup, shapetype, st_simplifypreservetopology(geom, 78270 / power(2, 1)) AS geom FROM globaladm1;

CREATE MATERIALIZED VIEW globaladm1_z0 AS
SELECT fid, shapegroup, shapetype, st_simplifypreservetopology(geom, 78270 / power(2, 0)) AS geom FROM globaladm1;


