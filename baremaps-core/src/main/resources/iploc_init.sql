-- Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
-- in compliance with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software distributed under the License
-- is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
-- or implied. See the License for the specific language governing permissions and limitations under
-- the License.
DROP TABLE IF EXISTS inetnum_locations;
CREATE TABLE IF NOT EXISTS inetnum_locations (
    id integer PRIMARY KEY,
    address text NOT NULL,
    ip_start blob,
    ip_end blob,
    latitude real,
    longitude real,
    network text,
    country text
);
CREATE INDEX inetnum_locations_ips ON inetnum_locations (ip_start,ip_end);