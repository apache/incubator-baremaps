-- Licensed under the Apache License, Version 2.0 (the License); you may not use this file except
-- in compliance with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software distributed under the License
-- is distributed on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
-- or implied. See the License for the specific language governing permissions and limitations under
-- the License.
DROP TABLE IF EXISTS osm_linestring CASCADE;

CREATE MATERIALIZED VIEW osm_linestring AS
SELECT id, tags, geom
FROM osm_ways
LEFT JOIN osm_member ON id = member_ref
WHERE ST_GeometryType(osm_ways.geom) = 'ST_LineString'
  AND tags != '{}'
  AND member_ref IS NULL;

CREATE INDEX IF NOT EXISTS osm_linestring_tags_index ON osm_linestring USING gin (tags);
CREATE INDEX IF NOT EXISTS osm_linestring_geom_index ON osm_linestring USING gist (geom);
