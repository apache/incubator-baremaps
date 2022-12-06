CREATE VIEW osm_nodes_z20 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}';

CREATE VIEW osm_nodes_z19 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}';

CREATE VIEW osm_nodes_z18 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}';

CREATE VIEW osm_nodes_z17 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}';

CREATE VIEW osm_nodes_z16 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}';

CREATE VIEW osm_nodes_z15 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}';

CREATE VIEW osm_nodes_z14 AS
SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}';

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
