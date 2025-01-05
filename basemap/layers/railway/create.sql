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

DROP MATERIALIZED VIEW IF EXISTS osm_railway CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_railway AS
SELECT id, tags, geom
FROM (SELECT min(id)                                        as id,
             jsonb_build_object(
                     'railway', tags -> 'railway',
                     'service', tags -> 'service',
                     'tunnel', tags -> 'tunnel'
             )                                              as tags,
             (st_dump(st_linemerge(st_collect(geom)))).geom as geom
      FROM osm_way
      WHERE tags ->> 'railway' IN ('light_rail', 'monorail', 'rail', 'subway', 'tram')
      GROUP BY tags -> 'railway', tags -> 'service', tags -> 'tunnel') AS mergedDirective
WITH NO DATA;

CREATE OR REPLACE VIEW osm_railway_z20 AS
SELECT id, tags, geom
FROM osm_railway;

CREATE OR REPLACE VIEW osm_railway_z19 AS
SELECT id, tags, geom
FROM osm_railway;

CREATE OR REPLACE VIEW osm_railway_z18 AS
SELECT id, tags, geom
FROM osm_railway;

CREATE OR REPLACE VIEW osm_railway_z17 AS
SELECT id, tags, geom
FROM osm_railway;

CREATE OR REPLACE VIEW osm_railway_z16 AS
SELECT id, tags, geom
FROM osm_railway;

CREATE OR REPLACE VIEW osm_railway_z15 AS
SELECT id, tags, geom
FROM osm_railway;

CREATE OR REPLACE VIEW osm_railway_z14 AS
SELECT id, tags, geom
FROM osm_railway;

CREATE OR REPLACE VIEW osm_railway_z13 AS
SELECT id, tags, geom
FROM osm_railway;

DROP MATERIALIZED VIEW IF EXISTS osm_railway_z12 CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_railway_z12 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 12)) AS geom FROM osm_railway) AS osm_railway
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 12)), 2))
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_railway_z11 CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_railway_z11 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 11)) AS geom FROM osm_railway) AS osm_railway
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 11)), 2))
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_railway_z10 CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_railway_z10 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 10)) AS geom FROM osm_railway) AS osm_railway
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 10)), 2))
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_railway_z9 CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_railway_z9 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 9)) AS geom FROM osm_railway) AS osm_railway
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 9)), 2))
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_railway_z8 CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_railway_z8 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 8)) AS geom FROM osm_railway) AS osm_railway
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 8)), 2))
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_railway_z7 CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_railway_z7 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 7)) AS geom FROM osm_railway) AS osm_railway
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 7)), 2))
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_railway_z6 CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_railway_z6 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 6)) AS geom FROM osm_railway) AS osm_railway
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 6)), 2))
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_railway_z5 CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_railway_z5 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 5)) AS geom FROM osm_railway) AS osm_railway
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 5)), 2))
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_railway_z4 CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_railway_z4 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 4)) AS geom FROM osm_railway) AS osm_railway
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 4)), 2))
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_railway_z3 CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_railway_z3 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 3)) AS geom FROM osm_railway) AS osm_railway
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 3)), 2))
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_railway_z2 CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_railway_z2 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 2)) AS geom FROM osm_railway) AS osm_railway
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 2)), 2))
WITH NO DATA;

DROP MATERIALIZED VIEW IF EXISTS osm_railway_z1 CASCADE;
CREATE MATERIALIZED VIEW IF NOT EXISTS osm_railway_z1 AS
SELECT id, tags, geom
FROM (SELECT id, tags, st_simplifypreservetopology(geom, 78270 / power(2, 1)) AS geom FROM osm_railway) AS osm_railway
WHERE geom IS NOT NULL
  AND (st_area(st_envelope(geom)) > power((78270 / power(2, 1)), 2))
WITH NO DATA;
