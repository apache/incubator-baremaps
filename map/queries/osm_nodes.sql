DROP INDEX IF EXISTS osm_nodes_tags_index;
DROP INDEX IF EXISTS osm_nodes_geom_index;

CREATE INDEX osm_nodes_tags_index ON osm_nodes USING gin (tags);
CREATE INDEX osm_nodes_geom_index ON osm_nodes USING spgist (geom);

DROP MATERIALIZED VIEW IF EXISTS osm_nodes_filtered CASCADE;

DROP VIEW IF EXISTS osm_nodes_z20 CASCADE;
DROP VIEW IF EXISTS osm_nodes_z19 CASCADE;
DROP VIEW IF EXISTS osm_nodes_z18 CASCADE;
DROP VIEW IF EXISTS osm_nodes_z17 CASCADE;
DROP VIEW IF EXISTS osm_nodes_z16 CASCADE;
DROP VIEW IF EXISTS osm_nodes_z15 CASCADE;
DROP VIEW IF EXISTS osm_nodes_z14 CASCADE;

DROP MATERIALIZED VIEW IF EXISTS osm_nodes_z13 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_nodes_z12 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_nodes_z11 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_nodes_z10 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_nodes_z9 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_nodes_z8 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_nodes_z7 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_nodes_z6 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_nodes_z5 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_nodes_z4 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_nodes_z3 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_nodes_z2 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_nodes_z1 CASCADE;

CREATE MATERIALIZED VIEW osm_nodes_filtered AS SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}';

CREATE VIEW osm_nodes_z20 AS SELECT id, tags, geom FROM osm_nodes_filtered;
CREATE VIEW osm_nodes_z19 AS SELECT id, tags, geom FROM osm_nodes_filtered;
CREATE VIEW osm_nodes_z18 AS SELECT id, tags, geom FROM osm_nodes_filtered;
CREATE VIEW osm_nodes_z17 AS SELECT id, tags, geom FROM osm_nodes_filtered;
CREATE VIEW osm_nodes_z16 AS SELECT id, tags, geom FROM osm_nodes_filtered;
CREATE VIEW osm_nodes_z15 AS SELECT id, tags, geom FROM osm_nodes_filtered;
CREATE VIEW osm_nodes_z14 AS SELECT id, tags, geom FROM osm_nodes_filtered;

CREATE MATERIALIZED VIEW osm_nodes_z13 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}' AND (tags ->> 'place' IN ('region', 'province', 'district', 'county', 'municipality', 'city', 'town', 'village', 'quarter', 'hamlet')) OR (tags ->> 'natural' IN ('peak', 'volcano')) OR (tags ->> 'highway' IN ('motorway_junction')) OR (tags ->> 'tourism' IN ('wilderness_hut')) OR (tags ->> 'waterway' IN ('waterfall')) OR (tags ->> 'natural' IN ('spring')) OR (tags ->> 'railway' IN ('level_crossing'));

CREATE MATERIALIZED VIEW osm_nodes_z12 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}' AND (tags ->> 'place' IN ('region', 'province', 'district', 'county', 'municipality', 'city', 'town', 'village')) OR (tags ->> 'natural' IN ('peak', 'volcano')) OR (tags ->> 'highway' IN ('motorway_junction')) OR (tags ->> 'tourism' IN ('wilderness_hut')) OR (tags ->> 'waterway' IN ('waterfall'));

CREATE MATERIALIZED VIEW osm_nodes_z11 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}' AND (tags ->> 'place' IN ('country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'town', 'village')) OR (tags ->> 'natural' IN ('peak', 'volcano')) OR (tags ->> 'highway' IN ('motorway_junction'));

CREATE MATERIALIZED VIEW osm_nodes_z10 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}' AND (tags ->> 'place' IN ('country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'town')) OR (tags ->> 'natural' IN ('peak', 'volcano')) OR (tags ->> 'highway' IN ('motorway_junction'));

CREATE MATERIALIZED VIEW osm_nodes_z9 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'town');

CREATE MATERIALIZED VIEW osm_nodes_z8 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'town');

CREATE MATERIALIZED VIEW osm_nodes_z7 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'city', 'sea', 'state', 'county');

CREATE MATERIALIZED VIEW osm_nodes_z6 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'city', 'sea', 'state', 'county');

CREATE MATERIALIZED VIEW osm_nodes_z5 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'city', 'sea', 'state', 'county');

CREATE MATERIALIZED VIEW osm_nodes_z4 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'city', 'sea');

CREATE MATERIALIZED VIEW osm_nodes_z3 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'city', 'sea');

CREATE MATERIALIZED VIEW osm_nodes_z2 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}' AND tags ->> 'place' IN ('country');

CREATE MATERIALIZED VIEW osm_nodes_z1 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}' AND tags ->> 'place' IN ('country');

CREATE INDEX IF NOT EXISTS osm_nodes_geom_filtered_index ON osm_nodes_filtered USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_nodes_geom_z13_index ON osm_nodes_z13 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_nodes_geom_z12_index ON osm_nodes_z12 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_nodes_geom_z11_index ON osm_nodes_z11 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_nodes_geom_z10_index ON osm_nodes_z10 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_nodes_geom_z9_index ON osm_nodes_z9 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_nodes_geom_z8_index ON osm_nodes_z8 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_nodes_geom_z7_index ON osm_nodes_z7 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_nodes_geom_z6_index ON osm_nodes_z6 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_nodes_geom_z5_index ON osm_nodes_z5 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_nodes_geom_z4_index ON osm_nodes_z4 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_nodes_geom_z3_index ON osm_nodes_z3 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_nodes_geom_z2_index ON osm_nodes_z2 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_nodes_geom_z1_index ON osm_nodes_z1 USING gist (geom);
