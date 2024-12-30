-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to you under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

CREATE OR REPLACE VIEW osm_point_z20 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}';

CREATE OR REPLACE VIEW osm_point_z19 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}';

CREATE OR REPLACE VIEW osm_point_z18 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}';

CREATE OR REPLACE VIEW osm_point_z17 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}';

CREATE OR REPLACE VIEW osm_point_z16 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}';

CREATE OR REPLACE VIEW osm_point_z15 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}';

CREATE OR REPLACE VIEW osm_point_z14 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}';

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z13 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}'
AND (tags ->> 'place' IN ('region', 'province', 'district', 'county', 'municipality', 'city', 'town', 'village', 'quarter', 'hamlet')) OR (tags ->> 'natural' IN ('peak', 'volcano')) OR (tags ->> 'highway' IN ('motorway_junction')) OR (tags ->> 'tourism' IN ('wilderness_hut')) OR (tags ->> 'waterway' IN ('waterfall')) OR (tags ->> 'natural' IN ('spring')) OR (tags ->> 'railway' IN ('level_crossing'));

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z12 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}' AND (tags ->> 'place' IN ('region', 'province', 'district', 'county', 'municipality', 'city', 'town', 'village')) OR (tags ->> 'natural' IN ('peak', 'volcano')) OR (tags ->> 'highway' IN ('motorway_junction')) OR (tags ->> 'tourism' IN ('wilderness_hut')) OR (tags ->> 'waterway' IN ('waterfall'));

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z11 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}' AND (tags ->> 'place' IN ('country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'town', 'village')) OR (tags ->> 'natural' IN ('peak', 'volcano')) OR (tags ->> 'highway' IN ('motorway_junction'));

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z10 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}' AND (tags ->> 'place' IN ('country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'town')) OR (tags ->> 'natural' IN ('peak', 'volcano')) OR (tags ->> 'highway' IN ('motorway_junction'));

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z9 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'town');

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z8 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'town');

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z7 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'city', 'sea', 'state', 'county');

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z6 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'city', 'sea', 'state', 'county');

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z5 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'city', 'sea', 'state', 'county');

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z4 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'city', 'sea');

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z3 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}' AND tags ->> 'place' IN ('country', 'city', 'sea');

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z2 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}' AND tags ->> 'place' IN ('country');

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_point_z1 AS
SELECT id, tags, geom
FROM osm_node
WHERE tags != '{}' AND tags ->> 'place' IN ('country');