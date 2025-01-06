-- Licensed under the Apache License, Version 2.0 (the License); you may not use this file except
-- in compliance with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software distributed under the License
-- is distributed on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
-- or implied. See the License for the specific language governing permissions and limitations under
-- the License.
DROP
    MATERIALIZED VIEW IF EXISTS esa_landcover CASCADE;

CREATE
    MATERIALIZED VIEW esa_landcover AS SELECT
        id AS id,
        jsonb_build_object(
            'landcover',
            class
        ) AS tags,
        geometry AS geom
    FROM
        low_shp;

CREATE
    INDEX IF NOT EXISTS esa_landcover_index ON
    esa_landcover
        USING SPGIST(geom);
