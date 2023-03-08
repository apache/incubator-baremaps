-- Licensed under the Apache License, Version 2.0 (the License); you may not use this file except
-- in compliance with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software distributed under the License
-- is distributed on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
-- or implied. See the License for the specific language governing permissions and limitations under
-- the License.
DROP MATERIALIZED VIEW IF EXISTS osm_polygon CASCADE;

CREATE MATERIALIZED VIEW osm_polygon AS
SELECT id, tags, geom
FROM osm_ways LEFT JOIN osm_ways_member ON id = way_id
WHERE ST_GeometryType(osm_ways.geom) = 'ST_Polygon'
  AND tags != '{}'
  AND way_id IS NULL
UNION
SELECT id, tags, geom
FROM osm_relations
WHERE ST_GeometryType(osm_relations.geom) = 'ST_Polygon'
  AND tags != '{}'
UNION
SELECT id, tags, (st_dump(geom)).geom as geom
FROM osm_relations
WHERE ST_GeometryType(osm_relations.geom) = 'ST_MultiPolygon'
  AND tags != '{}';
