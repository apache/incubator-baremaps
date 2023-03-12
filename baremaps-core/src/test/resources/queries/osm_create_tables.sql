-- Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
-- in compliance with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software distributed under the License
-- is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
-- or implied. See the License for the specific language governing permissions and limitations under
-- the License.
CREATE TABLE IF NOT EXISTS osm_headers
(
    replication_sequence_number bigint PRIMARY KEY,
    replication_timestamp       timestamp without time zone,
    replication_url             text,
    source                      text,
    writing_program             text
);
CREATE TABLE osm_nodes
(
    id        bigint PRIMARY KEY,
    version   int,
    uid       int,
    timestamp timestamp without time zone,
    changeset bigint,
    tags      jsonb,
    lon       float,
    lat       float,
    geom      geometry(point)
);
CREATE TABLE osm_ways
(
    id        bigint PRIMARY KEY,
    version   int,
    uid       int,
    timestamp timestamp without time zone,
    changeset bigint,
    tags      jsonb,
    nodes     bigint[],
    geom      geometry
);
CREATE TABLE osm_relations
(
    id           bigint PRIMARY KEY,
    version      int,
    uid          int,
    timestamp    timestamp without time zone,
    changeset    bigint,
    tags         jsonb,
    member_refs  bigint[],
    member_types int[],
    member_roles text[],
    geom         geometry
);