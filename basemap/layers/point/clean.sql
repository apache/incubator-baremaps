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

DROP INDEX IF EXISTS osm_point_tags_index;
DROP INDEX IF EXISTS osm_point_geom_index;

DROP VIEW IF EXISTS osm_point_z20 CASCADE;
DROP VIEW IF EXISTS osm_point_z19 CASCADE;
DROP VIEW IF EXISTS osm_point_z18 CASCADE;
DROP VIEW IF EXISTS osm_point_z17 CASCADE;
DROP VIEW IF EXISTS osm_point_z16 CASCADE;
DROP VIEW IF EXISTS osm_point_z15 CASCADE;
DROP VIEW IF EXISTS osm_point_z14 CASCADE;

DROP MATERIALIZED VIEW IF EXISTS osm_point_z13 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_point_z12 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_point_z11 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_point_z10 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_point_z9 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_point_z8 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_point_z7 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_point_z6 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_point_z5 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_point_z4 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_point_z3 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_point_z2 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_point_z1 CASCADE;