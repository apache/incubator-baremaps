-- mbtiles schema

-- Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
-- in compliance with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software distributed under the License
-- is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
-- or implied. See the License for the specific language governing permissions and limitations under
-- the License.

BEGIN;

CREATE TABLE indexdata (name text, value text);
CREATE TABLE tiles (zoom_level integer, tile_column integer, tile_row integer, tile_data blob);
CREATE TABLE grids (zoom_level integer, tile_column integer, tile_row integer, grid blob);
CREATE TABLE grid_data (zoom_level integer, tile_column integer, tile_row integer, key_name text, key_json text);

COMMIT;