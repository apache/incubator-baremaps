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
    MATERIALIZED VIEW IF EXISTS osm_coastline CASCADE;

DROP
    MATERIALIZED VIEW IF EXISTS osm_coastline_simplified CASCADE;

CREATE
    MATERIALIZED VIEW osm_coastline AS SELECT
        ROW_NUMBER() OVER() AS id,
        '{"ocean":"water"}'::jsonb AS tags,
        st_setsrid(
            geometry,
            3857
        ) AS geom
    FROM
        water_polygons_shp;

CREATE
    MATERIALIZED VIEW osm_coastline_simplified AS SELECT
        id,
        tags,
        st_simplifypreservetopology(
            geom,
            300
        ) AS geom
    FROM
        osm_coastline
    WHERE
        ST_Area(geom)> 300000;

CREATE
    INDEX IF NOT EXISTS osm_coastline_index ON
    osm_coastline
        USING SPGIST(geom);

CREATE
    INDEX IF NOT EXISTS osm_coastline_simplified_index ON
    osm_coastline_simplified
        USING SPGIST(geom);
