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
DROP MATERIALIZED VIEW IF EXISTS osm_waterway CASCADE;

CREATE MATERIALIZED VIEW osm_waterway AS
WITH
    -- Filter the linestrings
    filtered AS (
        SELECT
                tags -> 'waterway' AS waterway,
                geom AS geom
        FROM osm_linestring
        WHERE tags ->> 'waterway' IN ('river', 'stream', 'canal', 'drain', 'ditch')
        AND NOT tags ? 'intermittent'
    ),
    -- Cluster the linestrings
    clustered AS (
        SELECT
            waterway AS waterway,
            geom as geom,
            ST_ClusterDBSCAN(geom, 0, 1) OVER (PARTITION BY waterway) AS cluster
        FROM
            filtered
    ),
    -- Merge the linestrings into a single geometry per cluster
    merged AS (
        SELECT
            waterway AS waterway,
            ST_LineMerge(ST_Collect(geom)) AS geom
        FROM
            clustered
        GROUP BY
            waterway, cluster
    ),
    -- Explode the merged linestrings into individual linestrings
    exploded AS (
        SELECT
            waterway AS waterway,
            (ST_Dump(geom)).geom AS geom
        FROM
            merged
    )
SELECT
            row_number() OVER () AS id,
            jsonb_build_object('waterway', waterway) AS tags,
            geom AS geom
FROM exploded;

CREATE INDEX IF NOT EXISTS osm_waterway_tags_index ON osm_waterway USING gin (tags);
CREATE INDEX IF NOT EXISTS osm_waterway_geom_index ON osm_waterway USING gist (geom);
