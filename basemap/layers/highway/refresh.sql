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
DROP
    INDEX IF EXISTS osm_highway_filtered_geom;

REFRESH MATERIALIZED VIEW osm_highway_filtered;

CREATE
    INDEX IF NOT EXISTS osm_highway_filtered_geom ON
    osm_highway_filtered
        USING GIST(geom);

DROP
    INDEX IF EXISTS osm_highway_clustered_geom;

REFRESH MATERIALIZED VIEW osm_highway_clustered;

CREATE
    INDEX IF NOT EXISTS osm_highway_clustered_geom ON
    osm_highway_clustered
        USING GIST(geom);

DROP
    INDEX IF EXISTS osm_highway_simplified_geom;

REFRESH MATERIALIZED VIEW osm_highway_simplified;

CREATE
    INDEX IF NOT EXISTS osm_highway_simplified_geom ON
    osm_highway_simplified
        USING GIST(geom);

DROP
    INDEX IF EXISTS osm_highway_z12_geom_idx;

REFRESH MATERIALIZED VIEW osm_highway_z12;

CREATE
    INDEX IF NOT EXISTS osm_highway_z12_geom_idx ON
    osm_highway_z12
        USING GIST(geom);

DROP
    INDEX IF EXISTS osm_highway_z11_geom_idx;

REFRESH MATERIALIZED VIEW osm_highway_z11;

CREATE
    INDEX IF NOT EXISTS osm_highway_z11_geom_idx ON
    osm_highway_z11
        USING GIST(geom);

DROP
    INDEX IF EXISTS osm_highway_z10_geom_idx;

REFRESH MATERIALIZED VIEW osm_highway_z10;

CREATE
    INDEX IF NOT EXISTS osm_highway_z10_geom_idx ON
    osm_highway_z10
        USING GIST(geom);

DROP
    INDEX IF EXISTS osm_highway_z9_geom_idx;

REFRESH MATERIALIZED VIEW osm_highway_z9;

CREATE
    INDEX IF NOT EXISTS osm_highway_z9_geom_idx ON
    osm_highway_z9
        USING GIST(geom);

DROP
    INDEX IF EXISTS osm_highway_z8_geom_idx;

REFRESH MATERIALIZED VIEW osm_highway_z8;

CREATE
    INDEX IF NOT EXISTS osm_highway_z8_geom_idx ON
    osm_highway_z8
        USING GIST(geom);

DROP
    INDEX IF EXISTS osm_highway_z7_geom_idx;

REFRESH MATERIALIZED VIEW osm_highway_z7;

CREATE
    INDEX IF NOT EXISTS osm_highway_z7_geom_idx ON
    osm_highway_z7
        USING GIST(geom);

DROP
    INDEX IF EXISTS osm_highway_z6_geom_idx;

REFRESH MATERIALIZED VIEW osm_highway_z6;

CREATE
    INDEX IF NOT EXISTS osm_highway_z6_geom_idx ON
    osm_highway_z6
        USING GIST(geom);

DROP
    INDEX IF EXISTS osm_highway_z5_geom_idx;

REFRESH MATERIALIZED VIEW osm_highway_z5;

CREATE
    INDEX IF NOT EXISTS osm_highway_z5_geom_idx ON
    osm_highway_z5
        USING GIST(geom);

DROP
    INDEX IF EXISTS osm_highway_z4_geom_idx;

REFRESH MATERIALIZED VIEW osm_highway_z4;

CREATE
    INDEX IF NOT EXISTS osm_highway_z4_geom_idx ON
    osm_highway_z4
        USING GIST(geom);

DROP
    INDEX IF EXISTS osm_highway_z3_geom_idx;

REFRESH MATERIALIZED VIEW osm_highway_z3;

CREATE
    INDEX IF NOT EXISTS osm_highway_z3_geom_idx ON
    osm_highway_z3
        USING GIST(geom);

DROP
    INDEX IF EXISTS osm_highway_z2_geom_idx;

REFRESH MATERIALIZED VIEW osm_highway_z2;

CREATE
    INDEX IF NOT EXISTS osm_highway_z2_geom_idx ON
    osm_highway_z2
        USING GIST(geom);

DROP
    INDEX IF EXISTS osm_highway_z1_geom_idx;

REFRESH MATERIALIZED VIEW osm_highway_z1;

CREATE
    INDEX IF NOT EXISTS osm_highway_z1_geom_idx ON
    osm_highway_z1
        USING GIST(geom);
