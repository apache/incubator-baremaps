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

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_polygon AS
SELECT id, tags, geom
FROM osm_way LEFT JOIN osm_member ON id = member_ref
WHERE ST_GeometryType( osm_way.geom) = 'ST_Polygon'
  AND tags != '{}'
  AND member_ref IS NULL
UNION
SELECT id, tags, geom
FROM osm_relation
WHERE ST_GeometryType( osm_relation.geom) = 'ST_Polygon'
  AND tags != '{}'
UNION
SELECT id, tags, (st_dump(geom)).geom as geom
FROM osm_relation
WHERE ST_GeometryType( osm_relation.geom) = 'ST_MultiPolygon'
  AND tags != '{}';