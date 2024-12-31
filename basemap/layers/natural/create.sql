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

DROP MATERIALIZED VIEW IF EXISTS osm_natural_filtered CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_filtered AS
SELECT tags -> 'natural'                                       AS natural_value,
       st_simplifypreservetopology(geom, 78270 / power(2, 12)) AS geom
FROM osm_polygon
WHERE geom IS NOT NULL
  AND st_area(geom) > 78270 / power(2, 12) * 100
  AND tags ->> 'natural' IN
      ('grassland', 'heath', 'scrub', 'wood', 'bay', 'beach', 'glacier', 'mud', 'shingle', 'shoal', 'strait', 'water',
       'wetland', 'bare_rock', 'sand', 'scree')
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_natural_clustered CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_clustered AS
SELECT natural_value,
       geom,
       st_clusterdbscan(geom, 0, 0) OVER (PARTITION BY natural_value) AS cluster
FROM osm_natural_filtered
WHERE geom IS NOT NULL
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_natural CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural AS
WITH grouped AS (SELECT natural_value,
                        ST_Collect(geom) AS geom
                 FROM osm_natural_clustered
                 GROUP BY natural_value, cluster),
     buffered AS (SELECT natural_value,
                         ST_Buffer(geom, 0, 'join=mitre') AS geom
                  FROM grouped),
     exploded AS (SELECT natural_value,
                         (ST_Dump(geom)).geom AS geom
                  FROM buffered)
SELECT ROW_NUMBER() OVER ()                         AS id,
       JSONB_BUILD_OBJECT('natural', natural_value) AS tags,
       geom
FROM exploded
WITH NO DATA;

CREATE OR REPLACE VIEW osm_natural_z20 AS
SELECT id, tags, geom FROM osm_natural;

CREATE OR REPLACE VIEW osm_natural_z19 AS
SELECT id, tags, geom FROM osm_natural;

CREATE OR REPLACE VIEW osm_natural_z18 AS
SELECT id, tags, geom FROM osm_natural;

CREATE OR REPLACE VIEW osm_natural_z17 AS
SELECT id, tags, geom FROM osm_natural;

CREATE OR REPLACE VIEW osm_natural_z16 AS
SELECT id, tags, geom FROM osm_natural;

CREATE OR REPLACE VIEW osm_natural_z15 AS
SELECT id, tags, geom FROM osm_natural;

CREATE OR REPLACE VIEW osm_natural_z14 AS
SELECT id, tags, geom FROM osm_natural;

CREATE OR REPLACE VIEW osm_natural_z13 AS
SELECT id, tags, geom FROM osm_natural;

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z12;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z12 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 12)) AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 12), 2)
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z11;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z11 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 11)) AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 11), 2)
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z10;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z10 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 10)) AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 10), 2)
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z9;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z9 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 9)) AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 9), 2)
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z8;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z8 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 8)) AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 8), 2)
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z7;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z7 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 7)) AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 7), 2)
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z6;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z6 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 6)) AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 6), 2)
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z5;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z5 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 5)) AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 5), 2)
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z4;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z4 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 4)) AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 4), 2)
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z3;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z3 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 3)) AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 25 * power(78270 / power(2, 3), 2)
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z2;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z2 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 2)) AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 25 * power(78270 / power(2, 2), 2)
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_natural_z1;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_natural_z1 AS
SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 1)) AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 25 * power(78270 / power(2, 1), 2)
WITH NO DATA;
