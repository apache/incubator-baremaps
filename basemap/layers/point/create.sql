CREATE VIEW point AS
SELECT id, tags, geom
FROM osm_nodes
WHERE tags != '{}';

CREATE VIEW osm_point_z20 AS SELECT * FROM point;
CREATE VIEW osm_point_z19 AS SELECT * FROM point;
CREATE VIEW osm_point_z18 AS SELECT * FROM point;
CREATE VIEW osm_point_z17 AS SELECT * FROM point;
CREATE VIEW osm_point_z16 AS SELECT * FROM point;
CREATE VIEW osm_point_z15 AS SELECT * FROM point;
CREATE VIEW osm_point_z14 AS SELECT * FROM point;

CREATE MATERIALIZED VIEW osm_point_z13 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['region','province','district','county','municipality','city','town','village','quarter','hamlet'])
   OR (tags->>'natural') = ANY (ARRAY['peak','volcano','spring'])
   OR (tags->>'highway') = 'motorway_junction'
   OR (tags->>'tourism') = 'wilderness_hut'
   OR (tags->>'waterway') = 'waterfall'
   OR (tags->>'railway') = 'level_crossing';

CREATE MATERIALIZED VIEW osm_point_z12 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['region','province','district','county','municipality','city','town','village'])
   OR (tags->>'natural') = ANY (ARRAY['peak','volcano'])
   OR (tags->>'highway') = 'motorway_junction'
   OR (tags->>'tourism') = 'wilderness_hut'
   OR (tags->>'waterway') = 'waterfall';

CREATE MATERIALIZED VIEW osm_point_z11 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','state','region','province','district','county','municipality','city','town','village'])
   OR (tags->>'natural') = ANY (ARRAY['peak','volcano'])
   OR (tags->>'highway') = 'motorway_junction';

CREATE MATERIALIZED VIEW osm_point_z10 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','state','region','province','district','county','municipality','city','town'])
   OR (tags->>'natural') = ANY (ARRAY['peak','volcano'])
   OR (tags->>'highway') = 'motorway_junction';

CREATE MATERIALIZED VIEW osm_point_z9 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','state','region','province','district','county','municipality','city','town']);

CREATE MATERIALIZED VIEW osm_point_z8 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','state','region','province','district','county','municipality','city','town']);

CREATE MATERIALIZED VIEW osm_point_z7 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','city','sea','state','county']);

CREATE MATERIALIZED VIEW osm_point_z6 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','city','sea','state','county']);

CREATE MATERIALIZED VIEW osm_point_z5 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','city','sea','state','county']);

CREATE MATERIALIZED VIEW osm_point_z4 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','city','sea']);

CREATE MATERIALIZED VIEW osm_point_z3 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = ANY (ARRAY['country','city','sea']);

CREATE MATERIALIZED VIEW osm_point_z2 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = 'country';

CREATE MATERIALIZED VIEW osm_point_z1 AS
SELECT id, tags, geom
FROM point
WHERE (tags->>'place') = 'country';
