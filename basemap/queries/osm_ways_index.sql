-- Licensed under the Apache License, Version 2.0 (the License); you may not use this file except
-- in compliance with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software distributed under the License
-- is distributed on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
-- or implied. See the License for the specific language governing permissions and limitations under
-- the License.
CREATE INDEX IF NOT EXISTS osm_ways_z12_index ON osm_ways_z12 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_ways_z11_index ON osm_ways_z11 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_ways_z10_index ON osm_ways_z10 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_ways_z9_index ON osm_ways_z9 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_ways_z8_index ON osm_ways_z8 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_ways_z7_index ON osm_ways_z7 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_ways_z6_index ON osm_ways_z6 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_ways_z5_index ON osm_ways_z5 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_ways_z4_index ON osm_ways_z4 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_ways_z3_index ON osm_ways_z3 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_ways_z2_index ON osm_ways_z2 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS osm_ways_z1_index ON osm_ways_z1 USING SPGIST (geom);

CREATE INDEX osm_ways_member_index ON osm_ways_member(way_id);
