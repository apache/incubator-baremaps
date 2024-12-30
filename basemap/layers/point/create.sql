CREATE OR REPLACE VIEW osm_point AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}';

CREATE OR REPLACE VIEW osm_point_z20 AS
SELECT *
FROM point;

CREATE OR REPLACE VIEW osm_point_z19 AS
SELECT *
FROM point;

CREATE OR REPLACE VIEW osm_point_z18 AS
SELECT *
FROM point;

CREATE OR REPLACE VIEW osm_point_z17 AS
SELECT *
FROM point;

CREATE OR REPLACE VIEW osm_point_z16 AS
SELECT *
FROM point;

CREATE OR REPLACE VIEW osm_point_z15 AS
SELECT *
FROM point;

CREATE OR REPLACE VIEW osm_point_z14 AS
SELECT *
FROM point;

DROP MATERIALIZED VIEW IF EXISTS osm_point_z13;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z13 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['region','province','district','county','municipality','city','town','village','quarter','hamlet'])
   OR (tags->>'natural') = ANY (ARRAY['peak','volcano','spring'])
   OR (tags->>'highway') = 'motorway_junction'
   OR (tags->>'tourism') = 'wilderness_hut'
   OR (tags->>'waterway') = 'waterfall'
   OR (tags->>'railway') = 'level_crossing'
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_point_z12;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z12 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['region','province','district','county','municipality','city','town','village'])
   OR (tags->>'natural') = ANY (ARRAY['peak','volcano'])
   OR (tags->>'highway') = 'motorway_junction'
   OR (tags->>'tourism') = 'wilderness_hut'
   OR (tags->>'waterway') = 'waterfall'
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_point_z11;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z11 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','state','region','province','district','county','municipality','city','town','village'])
   OR (tags->>'natural') = ANY (ARRAY['peak','volcano'])
   OR (tags->>'highway') = 'motorway_junction'
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_point_z10;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z10 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','state','region','province','district','county','municipality','city','town'])
   OR (tags->>'natural') = ANY (ARRAY['peak','volcano'])
   OR (tags->>'highway') = 'motorway_junction'
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_point_z9;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z9 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','state','region','province','district','county','municipality','city','town'])
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_point_z8;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z8 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','state','region','province','district','county','municipality','city','town'])
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_point_z7;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z7 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','city','sea','state','county'])
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_point_z6;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z6 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','city','sea','state','county'])
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_point_z5;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z5 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','city','sea','state','county'])
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_point_z4;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z4 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','city','sea'])
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_point_z3;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z3 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','city','sea'])
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_point_z2;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z2 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = 'country'
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_point_z1;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z1 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = 'country'
WITH NO DATA;
