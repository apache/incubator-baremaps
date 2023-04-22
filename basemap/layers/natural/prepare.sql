-- Licensed under the Apache License, Version 2.0 (the License); you may not use this file except
-- in compliance with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software distributed under the License
-- is distributed on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
-- or implied. See the License for the specific language governing permissions and limitations under
-- the License.

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

CREATE MATERIALIZED VIEW osm_natural_medium_filtered AS
SELECT
    id,
    tags -> 'natural' as natural_value,
    st_buffer(st_simplifypreservetopology(geom, 78270 / power(2, 8)), 78270 / power(2, 8), 'join=mitre') AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 25 * power(78270 / power(2, 8), 2);
CREATE INDEX IF NOT EXISTS osm_natural_medium_filtered_geom_idx ON osm_natural_medium_filtered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_medium_filtered_tags_idx ON osm_natural_medium_filtered (natural_value);

CREATE MATERIALIZED VIEW osm_natural_medium_clustered AS
SELECT
    natural_value,
    geom,
    st_clusterdbscan(geom, 0, 0) OVER(PARTITION BY natural_value) AS cluster
FROM osm_natural_medium_filtered;
CREATE INDEX IF NOT EXISTS osm_natural_medium_clustered_geom_idx ON osm_natural_medium_clustered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_medium_clustered_tags_idx ON osm_natural_medium_clustered (natural_value);

CREATE MATERIALIZED VIEW osm_natural_medium_grouped AS
SELECT
    natural_value,
    cluster,
    st_collect(geom) AS geom
FROM osm_natural_medium_clustered
GROUP BY natural_value, cluster;

CREATE MATERIALIZED VIEW osm_natural_medium_buffered AS
SELECT
    natural_value,
    st_buffer(geom, -78270 / power(2, 8), 'join=mitre') AS geom
FROM osm_natural_medium_grouped;

CREATE MATERIALIZED VIEW osm_natural_medium_exploded AS
SELECT
    natural_value,
    (st_dump(geom)).geom AS geom
FROM osm_natural_medium_buffered;

CREATE MATERIALIZED VIEW osm_natural_medium AS
SELECT
    row_number() OVER () AS id,
    jsonb_build_object('natural', natural_value) AS tags,
    geom AS geom
FROM osm_natural_medium_buffered;
CREATE INDEX IF NOT EXISTS osm_natural_medium_geom_idx ON osm_natural_medium USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_medium_tags_idx ON osm_natural_medium USING GIN (tags);

CREATE MATERIALIZED VIEW osm_natural_small_filtered AS
SELECT
    id,
    tags -> 'natural' as natural_value,
    st_buffer(st_simplifypreservetopology(geom, 78270 / power(2, 4)), 78270 / power(2, 4), 'join=mitre') AS geom
FROM osm_natural
WHERE st_area(st_envelope(geom)) > 25 * power(78270 / power(2, 4), 2);
CREATE INDEX IF NOT EXISTS osm_natural_small_filtered_geom_idx ON osm_natural_small_filtered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_small_filtered_tags_idx ON osm_natural_small_filtered (natural_value);

CREATE MATERIALIZED VIEW osm_natural_small_clustered AS
SELECT
    natural_value,
    geom,
    st_clusterdbscan(geom, 0, 0) OVER(PARTITION BY natural_value) AS cluster
FROM osm_natural_small_filtered;
CREATE INDEX IF NOT EXISTS osm_natural_small_clustered_geom_idx ON osm_natural_small_clustered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_small_clustered_tags_idx ON osm_natural_small_clustered (natural_value);

CREATE MATERIALIZED VIEW osm_natural_small_grouped AS
SELECT
    natural_value,
    cluster,
    st_collect(geom) AS geom
FROM osm_natural_small_clustered
GROUP BY natural_value, cluster;

CREATE MATERIALIZED VIEW osm_natural_small_buffered AS
SELECT
    natural_value,
    st_buffer(geom, -78270 / power(2, 4), 'join=mitre') AS geom
FROM osm_natural_small_grouped;

CREATE MATERIALIZED VIEW osm_natural_small_exploded AS
SELECT
    natural_value,
    (st_dump(geom)).geom AS geom
FROM osm_natural_small_buffered;

CREATE MATERIALIZED VIEW osm_natural_small AS
SELECT
            row_number() OVER () AS id,
            jsonb_build_object('natural', natural_value) AS tags,
            geom AS geom
FROM osm_natural_small_buffered;
CREATE INDEX IF NOT EXISTS osm_natural_small_geom_idx ON osm_natural_small USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_natural_small_tags_idx ON osm_natural_small USING GIN (tags);
