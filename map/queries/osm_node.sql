DROP VIEW IF EXISTS osm_node CASCADE;

CREATE VIEW osm_node AS SELECT * FROM osm_nodes;

DROP INDEX IF EXISTS osm_node_tags_index;
DROP INDEX IF EXISTS osm_node_geom_index;

CREATE INDEX osm_node_tags_index ON osm_nodes USING gin (tags);
CREATE INDEX osm_node_geom_index ON osm_nodes USING spgist (geom);

CREATE MATERIALIZED VIEW osm_node_z1 AS
SELECT id, tags, geom FROM osm_node WHERE tags != '{}' AND tags ->> 'place' IN ('country');

CREATE MATERIALIZED VIEW osm_node_z2 AS
SELECT id, tags, geom FROM osm_node WHERE tags != '{}' AND tags ->> 'place' IN ('country');

CREATE MATERIALIZED VIEW osm_node_z3 AS
SELECT id, tags, geom FROM osm_node WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'city', 'sea');

CREATE MATERIALIZED VIEW osm_node_z4 AS
SELECT id, tags, geom FROM osm_node WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'city', 'sea');

CREATE MATERIALIZED VIEW osm_node_z5 AS
SELECT id, tags, geom FROM osm_node WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'city', 'sea', 'state', 'county');

CREATE MATERIALIZED VIEW osm_node_z6 AS
SELECT id, tags, geom FROM osm_node WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'city', 'sea', 'state', 'county');

CREATE MATERIALIZED VIEW osm_node_z7 AS
SELECT id, tags, geom FROM osm_node WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'city', 'sea', 'state', 'county');

CREATE MATERIALIZED VIEW osm_node_z8 AS
SELECT id, tags, geom FROM osm_node WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'town');

CREATE MATERIALIZED VIEW osm_node_z9 AS
SELECT id, tags, geom FROM osm_node WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'town');

CREATE MATERIALIZED VIEW osm_node_z10 AS
SELECT id, tags, geom FROM osm_node WHERE tags != '{}' AND (tags ->> 'place' IN ('country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'town')) OR (tags ->> 'natural' IN ('peak', 'volcano')) OR (tags ->> 'highway' IN ('motorway_junction'));

CREATE MATERIALIZED VIEW osm_node_z11 AS
SELECT id, tags, geom FROM osm_node WHERE tags != '{}' AND (tags ->> 'place' IN ('country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'town', 'village')) OR (tags ->> 'natural' IN ('peak', 'volcano')) OR (tags ->> 'highway' IN ('motorway_junction'));

CREATE MATERIALIZED VIEW osm_node_z12 AS
SELECT id, tags, geom FROM osm_node WHERE tags != '{}' AND (tags ->> 'place' IN ('region', 'province', 'district', 'county', 'municipality', 'city', 'town', 'village')) OR (tags ->> 'natural' IN ('peak', 'volcano')) OR (tags ->> 'highway' IN ('motorway_junction')) OR (tags ->> 'tourism' IN ('wilderness_hut')) OR (tags ->> 'waterway' IN ('waterfall'));

CREATE MATERIALIZED VIEW osm_node_z13 AS
SELECT id, tags, geom FROM osm_node WHERE tags != '{}' AND (tags ->> 'place' IN ('region', 'province', 'district', 'county', 'municipality', 'city', 'town', 'village', 'quarter', 'hamlet')) OR (tags ->> 'natural' IN ('peak', 'volcano')) OR (tags ->> 'highway' IN ('motorway_junction')) OR (tags ->> 'tourism' IN ('wilderness_hut')) OR (tags ->> 'waterway' IN ('waterfall')) OR (tags ->> 'natural' IN ('spring')) OR (tags ->> 'railway' IN ('level_crossing'));

CREATE MATERIALIZED VIEW osm_node_z14 AS
SELECT id, tags, geom FROM osm_node WHERE tags != '{}';

CREATE VIEW osm_node_z15 AS SELECT id, tags, geom FROM osm_node_z14;
CREATE VIEW osm_node_z16 AS SELECT id, tags, geom FROM osm_node_z14;
CREATE VIEW osm_node_z17 AS SELECT id, tags, geom FROM osm_node_z14;
CREATE VIEW osm_node_z18 AS SELECT id, tags, geom FROM osm_node_z14;
CREATE VIEW osm_node_z19 AS SELECT id, tags, geom FROM osm_node_z14;
CREATE VIEW osm_node_z20 AS SELECT id, tags, geom FROM osm_node_z14;

CREATE INDEX IF NOT EXISTS osm_node_geom_z1_index ON osm_node_z1 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_node_geom_z2_index ON osm_node_z2 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_node_geom_z3_index ON osm_node_z3 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_node_geom_z4_index ON osm_node_z4 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_node_geom_z5_index ON osm_node_z5 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_node_geom_z6_index ON osm_node_z6 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_node_geom_z7_index ON osm_node_z7 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_node_geom_z8_index ON osm_node_z8 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_node_geom_z9_index ON osm_node_z9 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_node_geom_z10_index ON osm_node_z10 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_node_geom_z11_index ON osm_node_z11 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_node_geom_z12_index ON osm_node_z12 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_node_geom_z13_index ON osm_node_z13 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_node_geom_z14_index ON osm_node_z14 USING gist (geom);
