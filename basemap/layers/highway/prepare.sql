-- Licensed under the Apache License, Version 2.0 (the License); you may not use this file except
-- in compliance with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software distributed under the License
-- is distributed on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
-- or implied. See the License for the specific language governing permissions and limitations under
-- the License.
DROP MATERIALIZED VIEW IF EXISTS osm_highway CASCADE;

CREATE MATERIALIZED VIEW osm_highway AS
WITH
    -- Filter the linestrings
    filtered AS (
        SELECT
                tags -> 'highway' AS highway,
                geom AS geom
        FROM osm_linestring
        WHERE tags ->> 'highway' IN ('motorway', 'motorway_link', 'trunk', 'trunk_link', 'primary', 'primary_link', 'secondary', 'secondary_link', 'tertiary',  'tertiary_link', 'unclassified', 'residential')
    ),
    -- Cluster the linestrings by highway type
    clustered AS (
        SELECT
                highway AS highway,
                geom as geom,
                ST_ClusterDBSCAN(geom, 0, 1) OVER (PARTITION BY highway) AS cluster
        FROM
            filtered
    ),
    -- Merge the linestrings into a single geometry per cluster
    merged AS (
        SELECT
            highway AS highway,
            ST_LineMerge(ST_Collect(geom)) AS geom
        FROM
            clustered
        GROUP BY
            highway, cluster
    ),
    -- Explode the merged linestrings into individual linestrings
    exploded AS (
        SELECT
            highway AS highway,
            (ST_Dump(geom)).geom AS geom
        FROM
            merged
    )
SELECT
    row_number() OVER () AS id,
    jsonb_build_object('highway', highway) AS tags,
    geom AS geom
FROM exploded;

CREATE INDEX IF NOT EXISTS osm_highway_geom_index ON osm_highway USING SPGIST (geom);
