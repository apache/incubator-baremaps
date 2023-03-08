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
SELECT id, tags, geom
FROM (
    SELECT
        min(id) as id,
        jsonb_build_object('highway', tags -> 'highway', 'ref', tags -> 'ref') as tags,
        (st_dump(st_linemerge(st_collect(geom)))).geom as geom
    FROM osm_linestring
    WHERE tags ->> 'highway' IN ('motorway', 'motorway_link', 'trunk', 'trunk_link', 'primary', 'primary_link', 'secondary', 'secondary_link', 'tertiary',  'tertiary_link', 'unclassified', 'residential')
    AND tags ? 'ref'
    GROUP BY tags -> 'highway', tags -> 'ref'
    UNION ALL
    SELECT
        id,
        jsonb_build_object('highway', tags -> 'highway') as tags,
        geom
    FROM osm_linestring
    WHERE tags ->> 'highway' IN ('motorway', 'motorway_link', 'trunk', 'trunk_link', 'primary', 'primary_link', 'secondary', 'secondary_link', 'tertiary',  'tertiary_link', 'unclassified', 'residential')
    AND NOT (tags ? 'ref')
) AS merge;

CREATE INDEX IF NOT EXISTS osm_highway_geom_index ON osm_highway USING SPGIST (geom);
