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

DROP MATERIALIZED VIEW IF EXISTS osm_landuse_filtered CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_landuse_filtered AS
SELECT tags -> 'landuse'                                       AS landuse,
       st_simplifypreservetopology(geom, 78270 / power(2, 12)) AS geom
FROM osm_polygon
WHERE geom IS NOT NULL
  AND st_area(geom) > 78270 / power(2, 12) * 100
  AND tags ->> 'landuse' IN
      ('commercial', 'construction', 'industrial', 'residential', 'retail', 'farmland', 'forest', 'meadow',
       'greenhouse_horticulture', 'meadow', 'orchard', 'plant_nursery', 'vineyard', 'basin', 'salt_pond', 'brownfield',
       'cemetery', 'grass', 'greenfield', 'landfill', 'military', 'quarry', 'railway')
WITH NO DATA;

CREATE INDEX IF NOT EXISTS osm_landuse_filtered_geom_idx ON osm_landuse_filtered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_landuse_filtered_tags_idx ON osm_landuse_filtered (landuse);

DROP MATERIALIZED VIEW IF EXISTS osm_landuse_clustered CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_landuse_clustered AS
SELECT landuse,
       geom,
       st_clusterdbscan(geom, 0, 0) OVER (PARTITION BY landuse) AS cluster
FROM osm_landuse_filtered
WHERE geom IS NOT NULL
WITH NO DATA;

CREATE INDEX IF NOT EXISTS osm_landuse_clustered_geom_idx ON osm_landuse_clustered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_landuse_clustered_tags_idx ON osm_landuse_clustered (landuse);

DROP MATERIALIZED VIEW IF EXISTS osm_landuse CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_landuse AS
WITH grouped AS (SELECT landuse,
                        ST_Collect(geom) AS geom
                 FROM osm_landuse_clustered
                 GROUP BY landuse, cluster),
     buffered AS (SELECT landuse,
                         ST_Buffer(geom, 0, 'join=mitre') AS geom
                  FROM grouped),
     exploded AS (SELECT landuse,
                         (ST_Dump(geom)).geom AS geom
                  FROM buffered)
SELECT ROW_NUMBER() OVER ()                   AS id,
       JSONB_BUILD_OBJECT('landuse', landuse) AS tags,
       geom
FROM exploded
WITH NO DATA;

CREATE INDEX IF NOT EXISTS osm_landuse_geom_idx ON osm_landuse USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_landuse_tags_idx ON osm_landuse USING GIN (tags);

CREATE OR REPLACE VIEW osm_landuse_z20 AS
SELECT id, tags, geom FROM osm_landuse;

CREATE OR REPLACE VIEW osm_landuse_z19 AS
SELECT id, tags, geom FROM osm_landuse;

CREATE OR REPLACE VIEW osm_landuse_z18 AS
SELECT id, tags, geom FROM osm_landuse;

CREATE OR REPLACE VIEW osm_landuse_z17 AS
SELECT id, tags, geom FROM osm_landuse;

CREATE OR REPLACE VIEW osm_landuse_z16 AS
SELECT id, tags, geom FROM osm_landuse;

CREATE OR REPLACE VIEW osm_landuse_z15 AS
SELECT id, tags, geom FROM osm_landuse;

CREATE OR REPLACE VIEW osm_landuse_z14 AS
SELECT id, tags, geom FROM osm_landuse;

CREATE OR REPLACE VIEW osm_landuse_z13 AS
SELECT id, tags, geom FROM osm_landuse;

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_landuse_z12 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 12)) AS geom
FROM osm_landuse
WHERE st_area(st_envelope(geom)) > 25 * power(78270 / power(2, 12), 2)
WITH NO DATA;

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_landuse_z11 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 11)) AS geom
FROM osm_landuse
WHERE st_area(st_envelope(geom)) > 25 * power(78270 / power(2, 11), 2)
WITH NO DATA;

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_landuse_z10 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 10)) AS geom
FROM osm_landuse
WHERE st_area(st_envelope(geom)) > 25 * power(78270 / power(2, 10), 2)
WITH NO DATA;

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_landuse_z9 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 9)) AS geom
FROM osm_landuse
WHERE st_area(st_envelope(geom)) > 25 * power(78270 / power(2, 9), 2)
WITH NO DATA;

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_landuse_z8 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 8)) AS geom
FROM osm_landuse
WHERE st_area(st_envelope(geom)) > 25 * power(78270 / power(2, 8), 2)
WITH NO DATA;

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_landuse_z7 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 7)) AS geom
FROM osm_landuse
WHERE st_area(st_envelope(geom)) > 150 * power(78270 / power(2, 7), 2)
WITH NO DATA;

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_landuse_z6 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 6)) AS geom
FROM osm_landuse
WHERE st_area(st_envelope(geom)) > 150 * power(78270 / power(2, 6), 2)
WITH NO DATA;

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_landuse_z5 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 5)) AS geom
FROM osm_landuse
WHERE st_area(st_envelope(geom)) > 150 * power(78270 / power(2, 5), 2)
WITH NO DATA;

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_landuse_z4 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 4)) AS geom
FROM osm_landuse
WHERE st_area(st_envelope(geom)) > 150 * power(78270 / power(2, 4), 2)
WITH NO DATA;

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_landuse_z3 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 3)) AS geom
FROM osm_landuse
WHERE st_area(st_envelope(geom)) > 150 * power(78270 / power(2, 3), 2)
WITH NO DATA;

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_landuse_z2 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 2)) AS geom
FROM osm_landuse
WHERE st_area(st_envelope(geom)) > 150 * power(78270 / power(2, 2), 2)
WITH NO DATA;

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_landuse_z1 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 1)) AS geom
FROM osm_landuse
WHERE st_area(st_envelope(geom)) > 250 * power(78270 / power(2, 1), 2)
WITH NO DATA;