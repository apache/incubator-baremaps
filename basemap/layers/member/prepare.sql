-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to you under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
DROP MATERIALIZED VIEW IF EXISTS osm_member CASCADE;

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_member AS
SELECT DISTINCT member_ref as member_ref
FROM osm_relation, unnest(member_types, member_refs) AS way(member_type, member_ref)
WHERE geom IS NOT NULL
  AND member_type = 1
  AND tags ->> 'type' = 'multipolygon'
  AND NOT tags ->> 'natural' = 'coastline';

CREATE INDEX osm_member_index ON osm_member(member_ref);