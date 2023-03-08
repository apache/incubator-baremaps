-- Licensed under the Apache License, Version 2.0 (the License); you may not use this file except
-- in compliance with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software distributed under the License
-- is distributed on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
-- or implied. See the License for the specific language governing permissions and limitations under
-- the License.
CREATE INDEX CONCURRENTLY IF NOT EXISTS osm_ways_gin ON osm_ways USING gin (nodes);
CREATE INDEX CONCURRENTLY IF NOT EXISTS osm_relations_gin ON osm_relations USING gin (member_refs);
CREATE INDEX CONCURRENTLY IF NOT EXISTS osm_nodes_gix ON osm_nodes USING GIST (geom);
CREATE INDEX CONCURRENTLY IF NOT EXISTS osm_ways_gix ON osm_ways USING GIST (geom);
CREATE INDEX CONCURRENTLY IF NOT EXISTS osm_relations_gix ON osm_relations USING GIST (geom);