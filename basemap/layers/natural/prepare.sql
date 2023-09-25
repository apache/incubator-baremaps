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

-- ('grassland', 'heath', 'scrub', 'wood', 'bay', 'beach', 'glacier', 'mud', 'shingle', 'shoal', 'strait', 'water', 'wetland', 'bare_rock', 'sand', 'scree');

CREATE MATERIALIZED VIEW osm_natural_filtered AS
SELECT
    tags -> 'natural' AS natural_value,
    st_simplifypreservetopology(geom, 78270 / power(2, 12)) AS geom
FROM osm_polygon
WHERE geom IS NOT NULL
  AND st_area(geom) > 78270 / power(2, 12) * 100
  AND tags ->> 'natural' IN ('grassland', 'heath', 'scrub', 'wood', 'bay', 'beach', 'glacier', 'mud', 'shingle', 'shoal', 'strait', 'water', 'wetland', 'bare_rock', 'sand', 'scree');
CREATE INDEX IF NOT EXISTS osm_natural_filtered_geom_idx ON osm_natural_filtered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_filtered_tags_idx ON osm_natural_filtered (natural_value);

CREATE MATERIALIZED VIEW osm_natural_clustered AS
SELECT
    natural_value,
    geom,
    st_clusterdbscan(geom, 0, 0) OVER(PARTITION BY natural_value) AS cluster
FROM osm_natural_filtered
WHERE geom IS NOT NULL;
CREATE INDEX IF NOT EXISTS osm_natural_clustered_geom_idx ON osm_natural_clustered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_clustered_tags_idx ON osm_natural_clustered (natural_value);

CREATE MATERIALIZED VIEW osm_natural_grouped AS
SELECT
    natural_value,
    st_collect(geom) AS geom
FROM osm_natural_clustered
GROUP BY natural_value, cluster;

CREATE MATERIALIZED VIEW osm_natural_buffered AS
SELECT
    natural_value,
    st_buffer(geom, 0, 'join=mitre') AS geom
FROM osm_natural_grouped;

CREATE MATERIALIZED VIEW osm_natural_exploded AS
SELECT
    natural_value,
    (st_dump(geom)).geom AS geom
FROM osm_natural_buffered;

CREATE MATERIALIZED VIEW osm_natural AS
SELECT
    row_number() OVER () AS id,
    jsonb_build_object('natural', natural_value) AS tags,
    geom
FROM osm_natural_exploded;
CREATE INDEX IF NOT EXISTS osm_natural_geom_idx ON osm_natural_filtered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_tags_idx ON osm_natural_filtered USING GIN (natural_value);

-- XTRA LARGE
CREATE MATERIALIZED VIEW osm_natural_xl_filtered AS
SELECT
    id,
    tags -> 'natural' as natural_value,
    st_buffer(st_simplifypreservetopology(geom, 78270 / power(2, 8)), 78270 / power(2, 8), 'join=mitre') AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 25 * power(78270 / power(2, 8), 2);
CREATE INDEX IF NOT EXISTS osm_natural_xl_filtered_geom_idx ON osm_natural_xl_filtered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_xl_filtered_tags_idx ON osm_natural_xl_filtered (natural_value);

CREATE MATERIALIZED VIEW osm_natural_xl_clustered AS
SELECT
    natural_value,
    geom,
    st_clusterdbscan(geom, 0, 0) OVER(PARTITION BY natural_value) AS cluster
FROM osm_natural_xl_filtered;
CREATE INDEX IF NOT EXISTS osm_natural_xl_clustered_geom_idx ON osm_natural_xl_clustered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_xl_clustered_tags_idx ON osm_natural_xl_clustered (natural_value);

CREATE MATERIALIZED VIEW osm_natural_xl_grouped AS
SELECT
    natural_value,
    cluster,
    st_collect(geom) AS geom
FROM osm_natural_xl_clustered
GROUP BY natural_value, cluster;

CREATE MATERIALIZED VIEW osm_natural_xl_buffered AS
SELECT
    natural_value,
    st_buffer(geom, -78270 / power(2, 8), 'join=mitre') AS geom
FROM osm_natural_xl_grouped;

CREATE MATERIALIZED VIEW osm_natural_xl_exploded AS
SELECT
    natural_value,
    (st_dump(geom)).geom AS geom
FROM osm_natural_xl_buffered;

CREATE MATERIALIZED VIEW osm_natural_xl AS
SELECT
    row_number() OVER () AS id,
    jsonb_build_object('natural', natural_value) AS tags,
    geom AS geom
FROM osm_natural_xl_buffered;
CREATE INDEX IF NOT EXISTS osm_natural_xl_geom_idx ON osm_natural_xl USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_xl_tags_idx ON osm_natural_xl USING GIN (tags);

-- MEDIUM
CREATE MATERIALIZED VIEW osm_natural_m_filtered AS
SELECT
    id,
    tags -> 'natural' as natural_value,
    st_buffer(st_simplifypreservetopology(geom, 78270 / power(2, 5)), 78270 / power(2, 6), 'join=mitre') AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 25 * power(78270 / power(2, 6), 2);
CREATE INDEX IF NOT EXISTS osm_natural_m_filtered_geom_idx ON osm_natural_m_filtered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_m_filtered_tags_idx ON osm_natural_m_filtered (natural_value);

CREATE MATERIALIZED VIEW osm_natural_m_clustered AS
SELECT
    natural_value,
    geom,
    st_clusterdbscan(geom, 0, 0) OVER(PARTITION BY natural_value) AS cluster
FROM osm_natural_m_filtered;
CREATE INDEX IF NOT EXISTS osm_natural_m_clustered_geom_idx ON osm_natural_m_clustered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_m_clustered_tags_idx ON osm_natural_m_clustered (natural_value);

CREATE MATERIALIZED VIEW osm_natural_m_grouped AS
SELECT
    natural_value,
    cluster,
    st_collect(geom) AS geom
FROM osm_natural_m_clustered
GROUP BY natural_value, cluster;

CREATE MATERIALIZED VIEW osm_natural_m_buffered AS
SELECT
    natural_value,
    st_buffer(geom, 0.5 * -78270 / power(2, 6), 'join=mitre') AS geom
FROM osm_natural_m_grouped;

CREATE MATERIALIZED VIEW osm_natural_m_exploded AS
SELECT
    natural_value,
    (st_dump(geom)).geom AS geom
FROM osm_natural_m_buffered;

CREATE MATERIALIZED VIEW osm_natural_m AS
SELECT
    row_number() OVER () AS id,
    jsonb_build_object('natural', natural_value) AS tags,
    geom AS geom
FROM osm_natural_m_buffered;
CREATE INDEX IF NOT EXISTS osm_natural_m_geom_idx ON osm_natural_m USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_m_tags_idx ON osm_natural_m USING GIN (tags);

-- SMALL
CREATE MATERIALIZED VIEW osm_natural_s_filtered AS
SELECT
    id,
    tags -> 'natural' as natural_value,
    st_buffer(st_simplifypreservetopology(geom, 78270 / power(2, 5)), 78270 / power(2, 6), 'join=mitre') AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 25 * power(78270 / power(2, 6), 2);
CREATE INDEX IF NOT EXISTS osm_natural_s_filtered_geom_idx ON osm_natural_s_filtered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_s_filtered_tags_idx ON osm_natural_s_filtered (natural_value);

CREATE MATERIALIZED VIEW osm_natural_s_clustered AS
SELECT
    natural_value,
    geom,
    st_clusterdbscan(geom, 0, 0) OVER(PARTITION BY natural_value) AS cluster
FROM osm_natural_s_filtered;
CREATE INDEX IF NOT EXISTS osm_natural_s_clustered_geom_idx ON osm_natural_s_clustered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_s_clustered_tags_idx ON osm_natural_s_clustered (natural_value);

CREATE MATERIALIZED VIEW osm_natural_s_grouped AS
SELECT
    natural_value,
    cluster,
    st_collect(geom) AS geom
FROM osm_natural_s_clustered
GROUP BY natural_value, cluster;

CREATE MATERIALIZED VIEW osm_natural_s_buffered AS
SELECT
    natural_value,
    st_buffer(geom, 0.1 * -78270 / power(2, 6), 'join=mitre') AS geom
FROM osm_natural_s_grouped;

CREATE MATERIALIZED VIEW osm_natural_s_exploded AS
SELECT
    natural_value,
    (st_dump(geom)).geom AS geom
FROM osm_natural_s_buffered;

CREATE MATERIALIZED VIEW osm_natural_s AS
SELECT
    row_number() OVER () AS id,
    jsonb_build_object('natural', natural_value) AS tags,
    geom AS geom
FROM osm_natural_s_buffered;
CREATE INDEX IF NOT EXISTS osm_natural_s_geom_idx ON osm_natural_s USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_s_tags_idx ON osm_natural_s USING GIN (tags);
