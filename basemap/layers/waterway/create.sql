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
DROP
    MATERIALIZED VIEW IF EXISTS osm_waterway CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_waterway AS WITH filtered AS(
        SELECT
            tags -> 'waterway' AS waterway,
            geom AS geom
        FROM
            osm_linestring
        WHERE
            tags ->> 'waterway' IN(
                'river',
                'stream',
                'canal',
                'drain',
                'ditch'
            )
            AND NOT tags ? 'intermittent'
    ),
    clustered AS(
        SELECT
            waterway AS waterway,
            geom AS geom,
            ST_ClusterDBSCAN(
                geom,
                0,
                1
            ) OVER(
                PARTITION BY waterway
            ) AS cluster
        FROM
            filtered
    ),
    merged AS(
        SELECT
            waterway AS waterway,
            ST_LineMerge(
                ST_Collect(geom)
            ) AS geom
        FROM
            clustered
        GROUP BY
            waterway,
            cluster
    ),
    exploded AS(
        SELECT
            waterway AS waterway,
            (
                ST_Dump(geom)
            ).geom AS geom
        FROM
            merged
    ) SELECT
        ROW_NUMBER() OVER() AS id,
        jsonb_build_object(
            'waterway',
            waterway
        ) AS tags,
        geom AS geom
    FROM
        exploded WITH NO DATA;

CREATE
    OR REPLACE VIEW osm_waterway_z20 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_waterway;

CREATE
    OR REPLACE VIEW osm_waterway_z19 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_waterway;

CREATE
    OR REPLACE VIEW osm_waterway_z18 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_waterway;

CREATE
    OR REPLACE VIEW osm_waterway_z17 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_waterway;

CREATE
    OR REPLACE VIEW osm_waterway_z16 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_waterway;

CREATE
    OR REPLACE VIEW osm_waterway_z15 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_waterway;

CREATE
    OR REPLACE VIEW osm_waterway_z14 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_waterway;

CREATE
    OR REPLACE VIEW osm_waterway_z13 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_waterway;

DROP
    MATERIALIZED VIEW IF EXISTS osm_waterway_z12 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_waterway_z12 AS SELECT
        id,
        tags,
        geom
    FROM
        (
            SELECT
                id,
                tags,
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 12 )
                ) AS geom
            FROM
                osm_waterway
        ) AS osm_waterway
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 12 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_waterway_z11 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_waterway_z11 AS SELECT
        id,
        tags,
        geom
    FROM
        (
            SELECT
                id,
                tags,
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 11 )
                ) AS geom
            FROM
                osm_waterway
        ) AS osm_waterway
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 11 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_waterway_z10 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_waterway_z10 AS SELECT
        id,
        tags,
        geom
    FROM
        (
            SELECT
                id,
                tags,
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 10 )
                ) AS geom
            FROM
                osm_waterway
        ) AS osm_waterway
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 10 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_waterway_z9 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_waterway_z9 AS SELECT
        id,
        tags,
        geom
    FROM
        (
            SELECT
                id,
                tags,
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 9 )
                ) AS geom
            FROM
                osm_waterway
        ) AS osm_waterway
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 9 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_waterway_z8 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_waterway_z8 AS SELECT
        id,
        tags,
        geom
    FROM
        (
            SELECT
                id,
                tags,
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 8 )
                ) AS geom
            FROM
                osm_waterway
        ) AS osm_waterway
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 8 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_waterway_z7 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_waterway_z7 AS SELECT
        id,
        tags,
        geom
    FROM
        (
            SELECT
                id,
                tags,
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 7 )
                ) AS geom
            FROM
                osm_waterway
        ) AS osm_waterway
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 7 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_waterway_z6 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_waterway_z6 AS SELECT
        id,
        tags,
        geom
    FROM
        (
            SELECT
                id,
                tags,
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 6 )
                ) AS geom
            FROM
                osm_waterway
        ) AS osm_waterway
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 6 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_waterway_z5 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_waterway_z5 AS SELECT
        id,
        tags,
        geom
    FROM
        (
            SELECT
                id,
                tags,
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 5 )
                ) AS geom
            FROM
                osm_waterway
        ) AS osm_waterway
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 5 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_waterway_z4 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_waterway_z4 AS SELECT
        id,
        tags,
        geom
    FROM
        (
            SELECT
                id,
                tags,
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 4 )
                ) AS geom
            FROM
                osm_waterway
        ) AS osm_waterway
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 4 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_waterway_z3 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_waterway_z3 AS SELECT
        id,
        tags,
        geom
    FROM
        (
            SELECT
                id,
                tags,
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 3 )
                ) AS geom
            FROM
                osm_waterway
        ) AS osm_waterway
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 3 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_waterway_z2 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_waterway_z2 AS SELECT
        id,
        tags,
        geom
    FROM
        (
            SELECT
                id,
                tags,
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 2 )
                ) AS geom
            FROM
                osm_waterway
        ) AS osm_waterway
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 2 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_waterway_z1 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_waterway_z1 AS SELECT
        id,
        tags,
        geom
    FROM
        (
            SELECT
                id,
                tags,
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 1 )
                ) AS geom
            FROM
                osm_waterway
        ) AS osm_waterway
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 1 )), 2 )
        ) WITH NO DATA;
