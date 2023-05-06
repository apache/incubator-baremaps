-- Licensed under the Apache License, Version 2.0 (the License); you may not use this file except
-- in compliance with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software distributed under the License
-- is distributed on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
-- or implied. See the License for the specific language governing permissions and limitations under
-- the License.
CREATE INDEX IF NOT EXISTS osm_point_geom_z13_index ON osm_point_z13 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_point_geom_z12_index ON osm_point_z12 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_point_geom_z11_index ON osm_point_z11 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_point_geom_z10_index ON osm_point_z10 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_point_geom_z9_index ON osm_point_z9 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_point_geom_z8_index ON osm_point_z8 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_point_geom_z7_index ON osm_point_z7 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_point_geom_z6_index ON osm_point_z6 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_point_geom_z5_index ON osm_point_z5 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_point_geom_z4_index ON osm_point_z4 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_point_geom_z3_index ON osm_point_z3 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_point_geom_z2_index ON osm_point_z2 USING gist (geom);
CREATE INDEX IF NOT EXISTS osm_point_geom_z1_index ON osm_point_z1 USING gist (geom);