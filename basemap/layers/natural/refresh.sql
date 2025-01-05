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

-- Zoom level 12

DROP INDEX IF EXISTS osm_natural_z12_filtered_geom_idx;
DROP INDEX IF EXISTS osm_natural_z12_filtered_tags_idx;
REFRESH MATERIALIZED VIEW osm_natural_z12_filtered;
CREATE INDEX IF NOT EXISTS osm_natural_z12_filtered_geom_idx ON osm_natural_z12_filtered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_z12_filtered_tags_idx ON osm_natural_z12_filtered (tag);

DROP INDEX IF EXISTS osm_natural_z12_geom_idx;
DROP INDEX IF EXISTS osm_natural_z12_tags_idx;
REFRESH MATERIALIZED VIEW osm_natural_z12;
CREATE INDEX IF NOT EXISTS osm_natural_z12_geom_idx ON osm_natural_z12 USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_z12_tags_idx ON osm_natural_z12 USING GIN (tags);

-- Zoom level 11

DROP INDEX IF EXISTS osm_natural_z11_filtered_geom_idx;
DROP INDEX IF EXISTS osm_natural_z11_filtered_tags_idx;
REFRESH MATERIALIZED VIEW osm_natural_z11_filtered;
CREATE INDEX IF NOT EXISTS osm_natural_z11_filtered_geom_idx ON osm_natural_z11_filtered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_z11_filtered_tags_idx ON osm_natural_z11_filtered (tag);

DROP INDEX IF EXISTS osm_natural_z11_geom_idx;
DROP INDEX IF EXISTS osm_natural_z11_tags_idx;
REFRESH MATERIALIZED VIEW osm_natural_z11;
CREATE INDEX IF NOT EXISTS osm_natural_z11_geom_idx ON osm_natural_z11 USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_z11_tags_idx ON osm_natural_z11 USING GIN (tags);

-- Zoom level 10

DROP INDEX IF EXISTS osm_natural_z10_filtered_geom_idx;
DROP INDEX IF EXISTS osm_natural_z10_filtered_tags_idx;
REFRESH MATERIALIZED VIEW osm_natural_z10_filtered;
CREATE INDEX IF NOT EXISTS osm_natural_z10_filtered_geom_idx ON osm_natural_z10_filtered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_z10_filtered_tags_idx ON osm_natural_z10_filtered (tag);

DROP INDEX IF EXISTS osm_natural_z10_geom_idx;
DROP INDEX IF EXISTS osm_natural_z10_tags_idx;
REFRESH MATERIALIZED VIEW osm_natural_z10;
CREATE INDEX IF NOT EXISTS osm_natural_z10_geom_idx ON osm_natural_z10 USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_z10_tags_idx ON osm_natural_z10 USING GIN (tags);

-- Zoom level 9

DROP INDEX IF EXISTS osm_natural_z9_geom_idx;
DROP INDEX IF EXISTS osm_natural_z9_tags_idx;
REFRESH MATERIALIZED VIEW osm_natural_z9;
CREATE INDEX IF NOT EXISTS osm_natural_z9_geom_idx ON osm_natural_z9 USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_z9_tags_idx ON osm_natural_z9 USING GIN (tags);

-- Zoom level 8

DROP INDEX IF EXISTS osm_natural_z8_geom_idx;
DROP INDEX IF EXISTS osm_natural_z8_tags_idx;
REFRESH MATERIALIZED VIEW osm_natural_z8;
CREATE INDEX IF NOT EXISTS osm_natural_z8_geom_idx ON osm_natural_z8 USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_z8_tags_idx ON osm_natural_z8 USING GIN (tags);

-- Zoom level 7

DROP INDEX IF EXISTS osm_natural_z7_geom_idx;
DROP INDEX IF EXISTS osm_natural_z7_tags_idx;
REFRESH MATERIALIZED VIEW osm_natural_z7;
CREATE INDEX IF NOT EXISTS osm_natural_z7_geom_idx ON osm_natural_z7 USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_z7_tags_idx ON osm_natural_z7 USING GIN (tags);

-- Zoom level 6

DROP INDEX IF EXISTS osm_natural_z6_geom_idx;
DROP INDEX IF EXISTS osm_natural_z6_tags_idx;
REFRESH MATERIALIZED VIEW osm_natural_z6;
CREATE INDEX IF NOT EXISTS osm_natural_z6_geom_idx ON osm_natural_z6 USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_z6_tags_idx ON osm_natural_z6 USING GIN (tags);

-- Zoom level 5

DROP INDEX IF EXISTS osm_natural_z5_geom_idx;
DROP INDEX IF EXISTS osm_natural_z5_tags_idx;
REFRESH MATERIALIZED VIEW osm_natural_z5;
CREATE INDEX IF NOT EXISTS osm_natural_z5_geom_idx ON osm_natural_z5 USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_z5_tags_idx ON osm_natural_z5 USING GIN (tags);

-- Zoom level 4

DROP INDEX IF EXISTS osm_natural_z4_geom_idx;
DROP INDEX IF EXISTS osm_natural_z4_tags_idx;
REFRESH MATERIALIZED VIEW osm_natural_z4;
CREATE INDEX IF NOT EXISTS osm_natural_z4_geom_idx ON osm_natural_z4 USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_z4_tags_idx ON osm_natural_z4 USING GIN (tags);

-- Zoom level 3

DROP INDEX IF EXISTS osm_natural_z3_geom_idx;
DROP INDEX IF EXISTS osm_natural_z3_tags_idx;
REFRESH MATERIALIZED VIEW osm_natural_z3;
CREATE INDEX IF NOT EXISTS osm_natural_z3_geom_idx ON osm_natural_z3 USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_z3_tags_idx ON osm_natural_z3 USING GIN (tags);

-- Zoom level 2

DROP INDEX IF EXISTS osm_natural_z2_geom_idx;
DROP INDEX IF EXISTS osm_natural_z2_tags_idx;
REFRESH MATERIALIZED VIEW osm_natural_z2;
CREATE INDEX IF NOT EXISTS osm_natural_z2_geom_idx ON osm_natural_z2 USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_z2_tags_idx ON osm_natural_z2 USING GIN (tags);

-- Zoom level 1

DROP INDEX IF EXISTS osm_natural_z1_geom_idx;
DROP INDEX IF EXISTS osm_natural_z1_tags_idx;
REFRESH MATERIALIZED VIEW osm_natural_z1;
CREATE INDEX IF NOT EXISTS osm_natural_z1_geom_idx ON osm_natural_z1 USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_z1_tags_idx ON osm_natural_z1 USING GIN (tags);
