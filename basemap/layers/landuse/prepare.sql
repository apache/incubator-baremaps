-- Licensed under the Apache License, Version 2.0 (the License); you may not use this file except
-- in compliance with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software distributed under the License
-- is distributed on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
-- or implied. See the License for the specific language governing permissions and limitations under
-- the License.
CREATE MATERIALIZED VIEW osm_landuse_filtered AS
SELECT
    tags -> 'landuse' AS landuse,
    st_simplifypreservetopology(geom, 78270 / power(2, 12)) AS geom
FROM osm_polygon
WHERE geom IS NOT NULL
  AND st_area(geom) > 78270 / power(2, 12) * 100
  AND tags ->> 'landuse' IN ('residential', 'farmland', 'forest', 'meadow', 'orchard', 'vineyard', 'salt_pond', 'water');
CREATE INDEX IF NOT EXISTS osm_landuse_filtered_geom_idx ON osm_landuse_filtered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_landuse_filtered_tags_idx ON osm_landuse_filtered (landuse);

CREATE MATERIALIZED VIEW osm_landuse_clustered AS
SELECT
    landuse,
    geom,
    st_clusterdbscan(geom, 0, 0) OVER(PARTITION BY landuse) AS cluster
FROM osm_landuse_filtered
WHERE geom IS NOT NULL;
CREATE INDEX IF NOT EXISTS osm_landuse_clustered_geom_idx ON osm_landuse_clustered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_landuse_clustered_tags_idx ON osm_landuse_clustered (landuse);

CREATE MATERIALIZED VIEW osm_landuse_grouped AS
SELECT
    landuse,
    st_collect(geom) AS geom
FROM osm_landuse_clustered
GROUP BY landuse, cluster;

CREATE MATERIALIZED VIEW osm_landuse_buffered AS
SELECT
    landuse,
    st_buffer(geom, 0, 'join=mitre') AS geom
FROM osm_landuse_grouped;

CREATE MATERIALIZED VIEW osm_landuse_exploded AS
SELECT
    landuse,
    (st_dump(geom)).geom AS geom
FROM osm_landuse_buffered;

CREATE MATERIALIZED VIEW osm_landuse AS
SELECT
    row_number() OVER () AS id,
    jsonb_build_object('landuse', landuse) AS tags,
    geom
FROM osm_landuse_exploded;

-- XTRA LARGE
CREATE MATERIALIZED VIEW osm_landuse_xl_filtered AS
SELECT
    id,
    tags -> 'landuse' as landuse,
    st_buffer(st_simplifypreservetopology(geom, 78270 / power(2, 8)), 78270 / power(2, 8), 'join=mitre') AS geom
FROM osm_landuse
WHERE st_area(st_envelope(geom)) > 25 * power(78270 / power(2, 8), 2);

CREATE MATERIALIZED VIEW osm_landuse_xl_clustered AS
SELECT
    landuse,
    geom,
    st_clusterdbscan(geom, 0, 0) OVER(PARTITION BY landuse) AS cluster
FROM osm_landuse_xl_filtered;

CREATE MATERIALIZED VIEW osm_landuse_xl_grouped AS
SELECT
    landuse,
    cluster,
    st_collect(geom) AS geom
FROM osm_landuse_xl_clustered
GROUP BY landuse, cluster;

CREATE MATERIALIZED VIEW osm_landuse_xl_buffered AS
SELECT
    landuse,
    st_buffer(geom, -78270 / power(2, 8), 'join=mitre') AS geom
FROM osm_landuse_xl_grouped;

CREATE MATERIALIZED VIEW osm_landuse_xl_exploded AS
SELECT
    landuse,
    (st_dump(geom)).geom AS geom
FROM osm_landuse_xl_buffered;

CREATE MATERIALIZED VIEW osm_landuse_xl AS
SELECT
    row_number() OVER () AS id,
    jsonb_build_object('landuse', landuse) AS tags,
    geom AS geom
FROM osm_landuse_xl_buffered;

-- LARGE
CREATE MATERIALIZED VIEW osm_landuse_l_filtered AS
SELECT
    id,
    tags -> 'landuse' as landuse,
    st_buffer(st_simplifypreservetopology(geom, 78270 / power(2, 6)), 78270 / power(2, 7), 'join=mitre') AS geom
FROM osm_landuse
WHERE st_area(st_envelope(geom)) > 5 * power(78270 / power(2, 7), 2);

CREATE MATERIALIZED VIEW osm_landuse_l_clustered AS
SELECT
    landuse,
    geom,
    st_clusterdbscan(geom, 0, 0) OVER(PARTITION BY landuse) AS cluster
FROM osm_landuse_l_filtered;

CREATE MATERIALIZED VIEW osm_landuse_l_grouped AS
SELECT
    landuse,
    cluster,
    st_collect(geom) AS geom
FROM osm_landuse_l_clustered
GROUP BY landuse, cluster;

CREATE MATERIALIZED VIEW osm_landuse_l_buffered AS
SELECT
    landuse,
    st_buffer(geom, 0.5 * -78270 / power(2, 7), 'join=mitre') AS geom
FROM osm_landuse_l_grouped;

CREATE MATERIALIZED VIEW osm_landuse_l_exploded AS
SELECT
    landuse,
    (st_dump(geom)).geom AS geom
FROM osm_landuse_l_buffered;

CREATE MATERIALIZED VIEW osm_landuse_l AS
SELECT
            row_number() OVER () AS id,
            jsonb_build_object('landuse', landuse) AS tags,
            geom AS geom
FROM osm_landuse_l_buffered;

-- MEDIUM
CREATE MATERIALIZED VIEW osm_landuse_m_filtered AS
SELECT
    id,
    tags -> 'landuse' as landuse,
    st_buffer(st_simplifypreservetopology(geom, 78270 / power(2, 5)), 78270 / power(2, 6), 'join=mitre') AS geom
FROM osm_landuse
WHERE st_area(st_envelope(geom)) > 10 * power(78270 / power(2, 6), 2);

CREATE MATERIALIZED VIEW osm_landuse_m_clustered AS
SELECT
    landuse,
    geom,
    st_clusterdbscan(geom, 0, 0) OVER(PARTITION BY landuse) AS cluster
FROM osm_landuse_m_filtered;

CREATE MATERIALIZED VIEW osm_landuse_m_grouped AS
SELECT
    landuse,
    cluster,
    st_collect(geom) AS geom
FROM osm_landuse_m_clustered
GROUP BY landuse, cluster;

CREATE MATERIALIZED VIEW osm_landuse_m_buffered AS
SELECT
    landuse,
    st_buffer(geom, 0.1 * -78270 / power(2, 6), 'join=mitre') AS geom
FROM osm_landuse_m_grouped;

CREATE MATERIALIZED VIEW osm_landuse_m_exploded AS
SELECT
    landuse,
    (st_dump(geom)).geom AS geom
FROM osm_landuse_m_buffered;

CREATE MATERIALIZED VIEW osm_landuse_m AS
SELECT
            row_number() OVER () AS id,
            jsonb_build_object('landuse', landuse) AS tags,
            geom AS geom
FROM osm_landuse_m_buffered;

-- SMALL
CREATE MATERIALIZED VIEW osm_landuse_s_filtered AS
SELECT
    id,
    tags -> 'landuse' as landuse,
    st_buffer(st_simplifypreservetopology(geom, 78270 / power(2, 4)), 78270 / power(2, 5), 'join=mitre') AS geom
FROM osm_landuse
WHERE st_area(st_envelope(geom)) > 15 * power(78270 / power(2, 5), 2);

CREATE MATERIALIZED VIEW osm_landuse_s_clustered AS
SELECT
    landuse,
    geom,
    st_clusterdbscan(geom, 0, 0) OVER(PARTITION BY landuse) AS cluster
FROM osm_landuse_s_filtered;

CREATE MATERIALIZED VIEW osm_landuse_s_grouped AS
SELECT
    landuse,
    cluster,
    st_collect(geom) AS geom
FROM osm_landuse_s_clustered
GROUP BY landuse, cluster;

CREATE MATERIALIZED VIEW osm_landuse_s_buffered AS
SELECT
    landuse,
    st_buffer(geom, 0, 'join=mitre') AS geom
FROM osm_landuse_s_grouped;

CREATE MATERIALIZED VIEW osm_landuse_s_exploded AS
SELECT
    landuse,
    (st_dump(geom)).geom AS geom
FROM osm_landuse_s_buffered;

CREATE MATERIALIZED VIEW osm_landuse_s AS
SELECT
            row_number() OVER () AS id,
            jsonb_build_object('landuse', landuse) AS tags,
            geom AS geom
FROM osm_landuse_s_buffered;
