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

DROP INDEX IF EXISTS osm_leisure_filtered_geom_idx;
REFRESH MATERIALIZED VIEW osm_leisure_filtered;
CREATE INDEX IF NOT EXISTS osm_leisure_filtered_geom_idx
    ON osm_leisure_filtered USING GIST (geom);

DROP INDEX IF EXISTS osm_leisure_clustered_geom_idx;
REFRESH MATERIALIZED VIEW osm_leisure_clustered;
CREATE INDEX IF NOT EXISTS osm_leisure_clustered_geom_idx
    ON osm_leisure_clustered USING GIST (geom);

DROP INDEX IF EXISTS osm_leisure_geom_idx;
REFRESH MATERIALIZED VIEW osm_leisure;
CREATE INDEX IF NOT EXISTS osm_leisure_geom_idx
    ON osm_leisure USING GIST (geom);

DROP INDEX IF EXISTS osm_leisure_z12_geom_idx;
REFRESH MATERIALIZED VIEW osm_leisure_z12;
CREATE INDEX IF NOT EXISTS osm_leisure_z12_geom_idx
    ON osm_leisure_z12 USING GIST (geom);

DROP INDEX IF EXISTS osm_leisure_z11_geom_idx;
REFRESH MATERIALIZED VIEW osm_leisure_z11;
CREATE INDEX IF NOT EXISTS osm_leisure_z11_geom_idx
    ON osm_leisure_z11 USING GIST (geom);

DROP INDEX IF EXISTS osm_leisure_z10_geom_idx;
REFRESH MATERIALIZED VIEW osm_leisure_z10;
CREATE INDEX IF NOT EXISTS osm_leisure_z10_geom_idx
    ON osm_leisure_z10 USING GIST (geom);

DROP INDEX IF EXISTS osm_leisure_z9_geom_idx;
REFRESH MATERIALIZED VIEW osm_leisure_z9;
CREATE INDEX IF NOT EXISTS osm_leisure_z9_geom_idx
    ON osm_leisure_z9 USING GIST (geom);

DROP INDEX IF EXISTS osm_leisure_z8_geom_idx;
REFRESH MATERIALIZED VIEW osm_leisure_z8;
CREATE INDEX IF NOT EXISTS osm_leisure_z8_geom_idx
    ON osm_leisure_z8 USING GIST (geom);

DROP INDEX IF EXISTS osm_leisure_z7_geom_idx;
REFRESH MATERIALIZED VIEW osm_leisure_z7;
CREATE INDEX IF NOT EXISTS osm_leisure_z7_geom_idx
    ON osm_leisure_z7 USING GIST (geom);

DROP INDEX IF EXISTS osm_leisure_z6_geom_idx;
REFRESH MATERIALIZED VIEW osm_leisure_z6;
CREATE INDEX IF NOT EXISTS osm_leisure_z6_geom_idx
    ON osm_leisure_z6 USING GIST (geom);

DROP INDEX IF EXISTS osm_leisure_z5_geom_idx;
REFRESH MATERIALIZED VIEW osm_leisure_z5;
CREATE INDEX IF NOT EXISTS osm_leisure_z5_geom_idx
    ON osm_leisure_z5 USING GIST (geom);

DROP INDEX IF EXISTS osm_leisure_z4_geom_idx;
REFRESH MATERIALIZED VIEW osm_leisure_z4;
CREATE INDEX IF NOT EXISTS osm_leisure_z4_geom_idx
    ON osm_leisure_z4 USING GIST (geom);

DROP INDEX IF EXISTS osm_leisure_z3_geom_idx;
REFRESH MATERIALIZED VIEW osm_leisure_z3;
CREATE INDEX IF NOT EXISTS osm_leisure_z3_geom_idx
    ON osm_leisure_z3 USING GIST (geom);

DROP INDEX IF EXISTS osm_leisure_z2_geom_idx;
REFRESH MATERIALIZED VIEW osm_leisure_z2;
CREATE INDEX IF NOT EXISTS osm_leisure_z2_geom_idx
    ON osm_leisure_z2 USING GIST (geom);

DROP INDEX IF EXISTS osm_leisure_z1_geom_idx;
REFRESH MATERIALIZED VIEW osm_leisure_z1;
CREATE INDEX IF NOT EXISTS osm_leisure_z1_geom_idx
    ON osm_leisure_z1 USING GIST (geom);