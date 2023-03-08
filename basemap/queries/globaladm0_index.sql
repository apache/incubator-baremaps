-- Licensed under the Apache License, Version 2.0 (the License); you may not use this file except
-- in compliance with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software distributed under the License
-- is distributed on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
-- or implied. See the License for the specific language governing permissions and limitations under
-- the License.
CREATE INDEX IF NOT EXISTS globaladm0_index ON globaladm0 USING SPGIST(geom);
CREATE INDEX IF NOT EXISTS globaladm0_z12_index ON globaladm0_z12 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS globaladm0_z11_index ON globaladm0_z11 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS globaladm0_z10_index ON globaladm0_z10 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS globaladm0_z9_index ON globaladm0_z9 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS globaladm0_z8_index ON globaladm0_z8 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS globaladm0_z7_index ON globaladm0_z7 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS globaladm0_z6_index ON globaladm0_z6 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS globaladm0_z5_index ON globaladm0_z5 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS globaladm0_z4_index ON globaladm0_z4 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS globaladm0_z3_index ON globaladm0_z3 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS globaladm0_z2_index ON globaladm0_z2 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS globaladm0_z1_index ON globaladm0_z1 USING SPGIST (geom);
CREATE INDEX IF NOT EXISTS globaladm0_z0_index ON globaladm0_z0 USING SPGIST (geom);
