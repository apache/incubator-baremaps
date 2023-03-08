-- Licensed under the Apache License, Version 2.0 (the License); you may not use this file except
-- in compliance with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software distributed under the License
-- is distributed on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
-- or implied. See the License for the specific language governing permissions and limitations under
-- the License.
DROP INDEX IF EXISTS osm_ways_tags_index;
DROP INDEX IF EXISTS osm_ways_geom_index;

DROP VIEW IF EXISTS osm_ways_z20 CASCADE;
DROP VIEW IF EXISTS osm_ways_z19 CASCADE;
DROP VIEW IF EXISTS osm_ways_z18 CASCADE;
DROP VIEW IF EXISTS osm_ways_z17 CASCADE;
DROP VIEW IF EXISTS osm_ways_z16 CASCADE;
DROP VIEW IF EXISTS osm_ways_z15 CASCADE;
DROP VIEW IF EXISTS osm_ways_z14 CASCADE;
DROP VIEW IF EXISTS osm_ways_z13 CASCADE;

DROP MATERIALIZED VIEW IF EXISTS osm_ways_z12 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_ways_z11 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_ways_z10 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_ways_z9 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_ways_z8 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_ways_z7 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_ways_z6 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_ways_z5 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_ways_z4 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_ways_z3 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_ways_z2 CASCADE;
DROP MATERIALIZED VIEW IF EXISTS osm_ways_z1 CASCADE;

DROP MATERIALIZED VIEW IF EXISTS osm_ways_member CASCADE;
