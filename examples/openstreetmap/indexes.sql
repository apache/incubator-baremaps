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
CREATE INDEX CONCURRENTLY IF NOT EXISTS osm_ways_gin ON osm_ways USING gin (nodes);
CREATE INDEX CONCURRENTLY IF NOT EXISTS osm_relations_gin ON osm_relations USING gin (member_refs);
CREATE INDEX CONCURRENTLY IF NOT EXISTS osm_nodes_gix ON osm_nodes USING GIST (geom);
CREATE INDEX CONCURRENTLY IF NOT EXISTS osm_ways_gix ON osm_ways USING GIST (geom);
CREATE INDEX CONCURRENTLY IF NOT EXISTS osm_relations_gix ON osm_relations USING GIST (geom);

with hfabbbbce as (select *
                   from osm_nodes
                   where ((tags ? 'aeroway') OR (tags ? 'waterway') OR (tags ? 'landuse') OR (tags ? 'railway') OR
                          (tags ? 'highway') OR (tags ? 'public_transport') OR (tags ? 'aerialway') OR
                          (tags ? 'geological') OR (tags ? 'building') OR (tags ? 'amenity') OR (tags ? 'craft') OR
                          (tags ? 'emergency') OR (tags ? 'historic') OR (tags ? 'leisure') OR (tags ? 'man_made') OR
                          (tags ? 'military') OR (tags ? 'natural') OR (tags ? 'office') OR (tags ? 'place') OR
                          (tags ? 'power') OR (tags ? 'route') OR (tags ? 'shop') OR (tags ? 'sport') OR
                          (tags ? 'telecom') OR (tags ? 'tourism'))
                     and st_intersects(geom, st_tileenvelope(14, 8625, 5750))),
     hf88736cf as (select *
                   from osm_ways
                   where ((tags ? 'aeroway') OR (tags ? 'waterway') OR (tags ? 'landuse') OR (tags ? 'railway') OR
                          (tags ? 'highway') OR (tags ? 'public_transport') OR (tags ? 'aerialway') OR
                          (tags ? 'geological') OR (tags ? 'building') OR (tags ? 'amenity') OR (tags ? 'craft') OR
                          (tags ? 'emergency') OR (tags ? 'historic') OR (tags ? 'leisure') OR (tags ? 'man_made') OR
                          (tags ? 'military') OR (tags ? 'natural') OR (tags ? 'office') OR (tags ? 'place') OR
                          (tags ? 'power') OR (tags ? 'route') OR (tags ? 'shop') OR (tags ? 'sport') OR
                          (tags ? 'telecom') OR (tags ? 'tourism'))
                     and st_intersects(geom, st_tileenvelope(14, 8625, 5750))),
     h633b0648 as (select *
                   from osm_relations
                   where ((tags ? 'aeroway' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'waterway' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'landuse' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'railway' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'highway' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'public_transport' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'aerialway' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'geological' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'building' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'amenity' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'craft' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'emergency' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'historic' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'leisure' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'man_made' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'military' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'natural' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'office' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'place' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'power' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'route' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'shop' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'sport' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'telecom' AND tags->>'type' = 'multipolygon') OR
                          (tags ? 'tourism' AND tags->>'type' = 'multipolygon'))
                     and st_intersects(geom, st_tileenvelope(14, 8625, 5750)))
select st_asmvt(target, 'aeroway', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'aeroway'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'aeroway'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'aeroway' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'waterway', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'waterway'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'waterway'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'waterway' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'landuse', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'landuse'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'landuse'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'landuse' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'railway', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'railway'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'railway'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'railway' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'highway', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'highway'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'highway'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'highway' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'public_transport', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'public_transport'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'public_transport'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'public_transport' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'aerialway', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'aerialway'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'aerialway'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'aerialway' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'geological', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'geological'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'geological'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'geological' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'building', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'building'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'building'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'building' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'amenity', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'amenity'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'amenity'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'amenity' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'craft', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'craft'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'craft'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'craft' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'emergency', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'emergency'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'emergency'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'emergency' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'historic', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'historic'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'historic'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'historic' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'leisure', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'leisure'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'leisure'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'leisure' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'man_made', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'man_made'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'man_made'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'man_made' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'military', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'military'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'military'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'military' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'natural', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'natural'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'natural'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'natural' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'office', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'office'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'office'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'office' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'place', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'place'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'place'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'place' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'power', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'power'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'power'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'power' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'route', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'route'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'route'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'route' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'shop', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'shop'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'shop'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'shop' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'sport', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'sport'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'sport'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'sport' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'telecom', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'telecom'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'telecom'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'telecom' AND tags->>'type' = 'multipolygon') as target
union all
select st_asmvt(target, 'tourism', 4096, 'geom', 'id')
from (select id                                                                                         as id,
             (tags || jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags,
             st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true)                       as geom
      from hfabbbbce
      where tags ? 'tourism'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from hf88736cf
      where tags ? 'tourism'
      union all
      select id as id, (tags || jsonb_build_object('geometry', lower (replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(14, 8625, 5750), 4096, 256, true) as geom
      from h633b0648
      where tags ? 'tourism' AND tags->>'type' = 'multipolygon') as target