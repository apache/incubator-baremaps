DROP VIEW IF EXISTS osm_point CASCADE;

CREATE VIEW osm_point AS SELECT id, tags, geom FROM osm_nodes WHERE tags != '{}';

CREATE VIEW osm_point_z20 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z19 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z18 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z17 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z16 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z15 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z14 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z13 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z12 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z11 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z10 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z9 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z8 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z7 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z6 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z5 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z4 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z3 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z2 AS SELECT id, tags, geom FROM osm_point;
CREATE VIEW osm_point_z1 AS SELECT id, tags, geom FROM osm_point;

--
--CREATE MATERIALIZED VIEW osm_point_z13 AS
--SELECT id, tags, geom
--FROM osm_point
--WHERE (tags ->> 'place' IN ('region', 'province', 'district', 'county', 'municipality', 'city', 'town', 'village', 'quarter', 'hamlet'))
--OR (tags ->> 'natural' IN ('peak', 'volcano'))
--OR (tags ->> 'highway' IN ('motorway_junction'))
--OR (tags ->> 'tourism' IN ('wilderness_hut'))
--OR (tags ->> 'waterway' IN ('waterfall'))
--OR (tags ->> 'natural' IN ('spring'))
--OR (tags ->> 'railway' IN ('level_crossing'));
--
--CREATE MATERIALIZED VIEW osm_point_z12 AS
--SELECT id, tags, geom
--FROM osm_point
--WHERE (tags ->> 'place' IN ('region', 'province', 'district', 'county', 'municipality', 'city', 'town', 'village'))
--OR (tags ->> 'natural' IN ('peak', 'volcano'))
--OR (tags ->> 'highway' IN ('motorway_junction'))
--OR (tags ->> 'tourism' IN ('wilderness_hut'))
--OR (tags ->> 'waterway' IN ('waterfall'));
--
--CREATE MATERIALIZED VIEW osm_point_z11 AS
--SELECT id, tags, geom
--FROM osm_point
--WHERE (tags ->> 'place' IN ('country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'town', 'village'))
--OR (tags ->> 'natural' IN ('peak', 'volcano'))
--OR (tags ->> 'highway' IN ('motorway_junction'));
--
--CREATE MATERIALIZED VIEW osm_point_z10 AS
--SELECT id, tags, geom
--FROM osm_point
--WHERE (tags ->> 'place' IN ('country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'town'))
--OR (tags ->> 'natural' IN ('peak', 'volcano'))
--OR (tags ->> 'highway' IN ('motorway_junction'));
--
--CREATE MATERIALIZED VIEW osm_point_z9 AS
--SELECT id, tags, geom
--FROM osm_point
--WHERE tags ->> 'place' IN ('country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'town');
--
--CREATE MATERIALIZED VIEW osm_point_z8 AS
--SELECT id, tags, geom
--FROM osm_point
--WHERE tags ->> 'place' IN ('country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'town');
--
--CREATE MATERIALIZED VIEW osm_point_z7 AS
--SELECT id, tags, geom
--FROM osm_point
--WHERE tags ->> 'place' IN ('country', 'city', 'sea', 'state', 'county');
--
--CREATE MATERIALIZED VIEW osm_point_z6 AS
--SELECT id, tags, geom
--FROM osm_point
--WHERE tags ->> 'place' IN ('country', 'city', 'sea', 'state', 'county');
--
--CREATE MATERIALIZED VIEW osm_point_z5 AS
--SELECT id, tags, geom
--FROM osm_point
--WHERE tags ->> 'place' IN ('country', 'city', 'sea', 'state', 'county');
--
--CREATE MATERIALIZED VIEW osm_point_z4 AS
--SELECT id, tags, geom
--FROM osm_point
--WHERE tags ->> 'place' IN ('country', 'city', 'sea');
--
--CREATE MATERIALIZED VIEW osm_point_z3 AS
--SELECT id, tags, geom
--FROM osm_point
--WHERE tags ->> 'place' IN ('country', 'city', 'sea');
--
--CREATE MATERIALIZED VIEW osm_point_z2 AS
--SELECT id, tags, geom
--FROM osm_point
--WHERE tags ->> 'place' IN ('country');
--
--CREATE  VIEW osm_point_z1 AS
--SELECT id, tags, geom
--FROM osm_point
--WHERE tags ->> 'place' IN ('country');
--
--CREATE INDEX IF NOT EXISTS osm_point_spgist ON osm_point USING SPGIST (geom);
--CREATE INDEX IF NOT EXISTS osm_point_z13_spgist ON osm_point_z13 USING SPGIST (geom);
--CREATE INDEX IF NOT EXISTS osm_point_z12_spgist ON osm_point_z12 USING SPGIST (geom);
--CREATE INDEX IF NOT EXISTS osm_point_z11_spgist ON osm_point_z11 USING SPGIST (geom);
--CREATE INDEX IF NOT EXISTS osm_point_z10_spgist ON osm_point_z10 USING SPGIST (geom);
--CREATE INDEX IF NOT EXISTS osm_point_z9_spgist ON osm_point_z9 USING SPGIST (geom);
--CREATE INDEX IF NOT EXISTS osm_point_z8_spgist ON osm_point_z8 USING SPGIST (geom);
--CREATE INDEX IF NOT EXISTS osm_point_z7_spgist ON osm_point_z7 USING SPGIST (geom);
--CREATE INDEX IF NOT EXISTS osm_point_z6_spgist ON osm_point_z6 USING SPGIST (geom);
--CREATE INDEX IF NOT EXISTS osm_point_z5_spgist ON osm_point_z5 USING SPGIST (geom);
--CREATE INDEX IF NOT EXISTS osm_point_z4_spgist ON osm_point_z4 USING SPGIST (geom);
--CREATE INDEX IF NOT EXISTS osm_point_z3_spgist ON osm_point_z3 USING SPGIST (geom);
--CREATE INDEX IF NOT EXISTS osm_point_z2_spgist ON osm_point_z2 USING SPGIST (geom);
--CREATE INDEX IF NOT EXISTS osm_point_z1_spgist ON osm_point_z1 USING SPGIST (geom);
