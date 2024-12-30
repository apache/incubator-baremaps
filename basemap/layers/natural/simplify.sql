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

CREATE OR REPLACE VIEW osm_natural_z20 AS
SELECT id, tags, geom FROM osm_natural;

CREATE OR REPLACE VIEW osm_natural_z19 AS
SELECT id, tags, geom FROM osm_natural;

CREATE OR REPLACE VIEW osm_natural_z18 AS
SELECT id, tags, geom FROM osm_natural;

CREATE OR REPLACE VIEW osm_natural_z17 AS
SELECT id, tags, geom FROM osm_natural;

CREATE OR REPLACE VIEW osm_natural_z16 AS
SELECT id, tags, geom FROM osm_natural;

CREATE OR REPLACE VIEW osm_natural_z15 AS
SELECT id, tags, geom FROM osm_natural;

CREATE OR REPLACE VIEW osm_natural_z14 AS
SELECT id, tags, geom FROM osm_natural;

CREATE OR REPLACE VIEW osm_natural_z13 AS
SELECT id, tags, geom FROM osm_natural;

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z12 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 12)) AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 12), 2);

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z11 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 11)) AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 11), 2);

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z10 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 10)) AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 10), 2);

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z9 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 9)) AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 9), 2);

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z8 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 8)) AS geom
FROM osm_natural_xl
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 8), 2);

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z7 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 7)) AS geom
FROM osm_natural_xl
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 7), 2);

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z6 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 6)) AS geom
FROM osm_natural_m
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 6), 2);

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z5 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 5)) AS geom
FROM osm_natural_s
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 5), 2);

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z4 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 4)) AS geom
FROM osm_natural_s
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 4), 2);

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z3 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 3)) AS geom
FROM osm_natural_s
WHERE st_area(st_envelope(geom)) > 25 * power(78270 / power(2, 3), 2);

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z2 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 2)) AS geom
FROM osm_natural_s
WHERE st_area(st_envelope(geom)) > 25 * power(78270 / power(2, 2), 2);

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z1 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 1)) AS geom
FROM osm_natural_s
WHERE st_area(st_envelope(geom)) > 25 * power(78270 / power(2, 1), 2);
