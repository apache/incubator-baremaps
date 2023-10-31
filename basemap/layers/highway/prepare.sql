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
DROP MATERIALIZED VIEW IF EXISTS osm_highway CASCADE;

CREATE MATERIALIZED VIEW osm_highway AS
WITH
    -- Filter the linestrings
    filtered AS (
        SELECT
                tags -> 'highway' AS highway,
                tags -> 'construction' AS construction,
                geom AS geom
        FROM osm_linestring
        WHERE tags ->> 'highway' IN ('motorway', 'motorway_link', 'trunk', 'trunk_link', 'primary', 'primary_link', 'secondary', 'secondary_link', 'tertiary', 'tertiary_link', 'unclassified', 'residential', 'construction')
        AND changeset != 1000000000 -- Exclude the linestrings that are part of the daylight distribution
    ),
    -- Cluster the linestrings by highway type
    clustered AS (
        SELECT
                highway AS highway,
                construction AS construction,
                geom as geom,
                ST_ClusterDBSCAN(geom, 0, 1) OVER (PARTITION BY highway) AS cluster
        FROM
            filtered
    ),
    -- Merge the linestrings into a single geometry per cluster
    merged AS (
        SELECT
            highway AS highway,
            construction AS construction,
            ST_LineMerge(ST_Collect(geom)) AS geom
        FROM
            clustered
        GROUP BY
            highway, construction, cluster
    ),
    -- Explode the merged linestrings into individual linestrings
    exploded AS (
        SELECT
            highway AS highway,
            construction AS construction,
            (ST_Dump(geom)).geom AS geom
        FROM
            merged
        UNION
        -- Add the linestrings that are part of the daylight distribution
        SELECT
                tags -> 'highway' AS highway,
                tags -> 'construction' AS construction,
                geom AS geom
        FROM osm_linestring
        WHERE tags ->> 'highway' IN ('motorway', 'motorway_link', 'trunk', 'trunk_link', 'primary', 'primary_link', 'secondary', 'secondary_link', 'tertiary', 'tertiary_link', 'unclassified', 'residential', 'construction')
          AND changeset = 1000000000
    )
SELECT
    row_number() OVER () AS id,
    jsonb_build_object('highway', highway, 'construction', construction) AS tags,
    geom AS geom
FROM exploded;

CREATE INDEX IF NOT EXISTS osm_highway_geom_index ON osm_highway USING SPGIST (geom);
