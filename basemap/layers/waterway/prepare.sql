-- Licensed under the Apache License, Version 2.0 (the License); you may not use this file except
-- in compliance with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software distributed under the License
-- is distributed on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
-- or implied. See the License for the specific language governing permissions and limitations under
-- the License.
DROP MATERIALIZED VIEW IF EXISTS osm_waterway CASCADE;

CREATE MATERIALIZED VIEW osm_waterway AS
SELECT id, tags, geom
FROM (
   SELECT
       min(id) as id,
       jsonb_build_object('waterway', tags -> 'waterway') as tags,
       (st_dump(st_linemerge(st_collect(geom)))).geom as geom
   FROM osm_ways
   WHERE tags ? 'waterway'
   GROUP BY tags -> 'waterway'
) AS merge;

CREATE INDEX IF NOT EXISTS osm_waterway_tags_index ON osm_waterway USING gin (tags);
CREATE INDEX IF NOT EXISTS osm_waterway_geom_index ON osm_waterway USING gist (geom);
