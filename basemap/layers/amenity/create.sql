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
-- Zoom levels 20 to 13
CREATE
    OR REPLACE VIEW osm_amenity AS SELECT
        id,
        tags,
        geom
    FROM
        osm_way
    WHERE
        tags ? 'amenity'
UNION SELECT
        id,
        jsonb_build_object(
            'amenity',
            tags -> 'amenity'
        ) AS tags,
        geom
    FROM
        osm_relation
    WHERE
        tags ? 'amenity';

CREATE
    OR REPLACE VIEW osm_amenity_z20 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_amenity;

CREATE
    OR REPLACE VIEW osm_amenity_z19 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_amenity;

CREATE
    OR REPLACE VIEW osm_amenity_z18 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_amenity;

CREATE
    OR REPLACE VIEW osm_amenity_z17 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_amenity;

CREATE
    OR REPLACE VIEW osm_amenity_z16 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_amenity;

CREATE
    OR REPLACE VIEW osm_amenity_z15 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_amenity;

CREATE
    OR REPLACE VIEW osm_amenity_z14 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_amenity;

CREATE
    OR REPLACE VIEW osm_amenity_z13 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_amenity;

-- Zoom level 12
DROP
    MATERIALIZED VIEW IF EXISTS osm_amenity_z12_filtered CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_amenity_z12_filtered AS SELECT
        tags -> 'amenity' AS tag,
        st_buffer(
            st_simplifypreservetopology(
                geom,
                78270 / POWER( 2, 12 )
            ),
            78270 / POWER( 2, 12 )* 1.1,
            'join=mitre'
        ) AS geom
    FROM
        osm_amenity
    WHERE
        geom IS NOT NULL
        AND NOT ST_IsEmpty(geom)
        AND st_area(geom)> POWER( 78270 / POWER( 2, 12 ), 2 )* 32 WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_amenity_z12 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_amenity_z12 AS WITH clustered AS(
        SELECT
            tag,
            geom,
            st_clusterdbscan(
                geom,
                0,
                0
            ) OVER(
                PARTITION BY tag
            ) AS cluster
        FROM
            osm_amenity_z12_filtered
    ) SELECT
        ROW_NUMBER() OVER() AS id,
        jsonb_build_object(
            'amenity',
            tag
        ) AS tags,
        st_simplifypreservetopology(
            (
                st_dump(
                    st_buffer(
                        st_collect(geom),
                        - 78270 / POWER( 2, 12 ),
                        'join=mitre'
                    )
                )
            ).geom,
            78270 / POWER( 2, 12 )
        ) AS geom
    FROM
        clustered
    WHERE
        geom IS NOT NULL
        AND NOT ST_IsEmpty(geom)
        AND st_area(geom)> POWER( 78270 / POWER( 2, 12 ), 2 )* 32
    GROUP BY
        tag,
        cluster WITH NO DATA;

-- Zoom level 11
DROP
    MATERIALIZED VIEW IF EXISTS osm_amenity_z11_filtered CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_amenity_z11_filtered AS SELECT
        tags -> 'amenity' AS tag,
        st_buffer(
            st_simplifypreservetopology(
                geom,
                78270 / POWER( 2, 11 )
            ),
            78270 / POWER( 2, 11 )* 1.1,
            'join=mitre'
        ) AS geom
    FROM
        osm_amenity_z12
    WHERE
        geom IS NOT NULL
        AND NOT ST_IsEmpty(geom)
        AND st_area(geom)> POWER( 78270 / POWER( 2, 11 ), 2 )* 32 WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_amenity_z11 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_amenity_z11 AS WITH clustered AS(
        SELECT
            tag,
            geom,
            st_clusterdbscan(
                geom,
                0,
                0
            ) OVER(
                PARTITION BY tag
            ) AS cluster
        FROM
            osm_amenity_z11_filtered
    ) SELECT
        ROW_NUMBER() OVER() AS id,
        jsonb_build_object(
            'amenity',
            tag
        ) AS tags,
        st_simplifypreservetopology(
            (
                st_dump(
                    st_buffer(
                        st_collect(geom),
                        - 78270 / POWER( 2, 11 ),
                        'join=mitre'
                    )
                )
            ).geom,
            78270 / POWER( 2, 11 )
        ) AS geom
    FROM
        clustered
    WHERE
        geom IS NOT NULL
        AND NOT ST_IsEmpty(geom)
        AND st_area(geom)> POWER( 78270 / POWER( 2, 11 ), 2 )* 32
    GROUP BY
        tag,
        cluster WITH NO DATA;

-- Zoom level 10
DROP
    MATERIALIZED VIEW IF EXISTS osm_amenity_z10_filtered CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_amenity_z10_filtered AS SELECT
        tags -> 'amenity' AS tag,
        st_buffer(
            st_simplifypreservetopology(
                geom,
                78270 / POWER( 2, 10 )
            ),
            78270 / POWER( 2, 10 )* 1.1,
            'join=mitre'
        ) AS geom
    FROM
        osm_amenity_z11
    WHERE
        geom IS NOT NULL
        AND NOT ST_IsEmpty(geom)
        AND st_area(geom)> POWER( 78270 / POWER( 2, 10 ), 2 )* 32 WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_amenity_z10 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_amenity_z10 AS WITH clustered AS(
        SELECT
            tag,
            geom,
            st_clusterdbscan(
                geom,
                0,
                0
            ) OVER(
                PARTITION BY tag
            ) AS cluster
        FROM
            osm_amenity_z10_filtered
    ) SELECT
        ROW_NUMBER() OVER() AS id,
        jsonb_build_object(
            'amenity',
            tag
        ) AS tags,
        st_simplifypreservetopology(
            (
                st_dump(
                    st_buffer(
                        st_collect(geom),
                        - 78270 / POWER( 2, 10 ),
                        'join=mitre'
                    )
                )
            ).geom,
            78270 / POWER( 2, 10 )
        ) AS geom
    FROM
        clustered
    WHERE
        geom IS NOT NULL
        AND NOT ST_IsEmpty(geom)
        AND st_area(geom)> POWER( 78270 / POWER( 2, 10 ), 2 )* 32
    GROUP BY
        tag,
        cluster WITH NO DATA;

-- Zoom level 9
DROP
    MATERIALIZED VIEW IF EXISTS osm_amenity_z9 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_amenity_z9 AS WITH filtered AS(
        SELECT
            tags -> 'amenity' AS tag,
            st_buffer(
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 9 )
                ),
                78270 / POWER( 2, 9 )* 1.1,
                'join=mitre'
            ) AS geom
        FROM
            osm_amenity_z10
        WHERE
            geom IS NOT NULL
            AND NOT ST_IsEmpty(geom)
            AND st_area(geom)> POWER( 78270 / POWER( 2, 9 ), 2 )* 32
    ),
    clustered AS(
        SELECT
            tag,
            geom,
            st_clusterdbscan(
                geom,
                0,
                0
            ) OVER(
                PARTITION BY tag
            ) AS cluster
        FROM
            filtered
    ) SELECT
        ROW_NUMBER() OVER() AS id,
        jsonb_build_object(
            'amenity',
            tag
        ) AS tags,
        st_simplifypreservetopology(
            (
                st_dump(
                    st_buffer(
                        st_collect(geom),
                        - 78270 / POWER( 2, 9 ),
                        'join=mitre'
                    )
                )
            ).geom,
            78270 / POWER( 2, 9 )
        ) AS geom
    FROM
        clustered
    WHERE
        geom IS NOT NULL
        AND NOT ST_IsEmpty(geom)
        AND st_area(geom)> POWER( 78270 / POWER( 2, 9 ), 2 )* 32
    GROUP BY
        tag,
        cluster WITH NO DATA;

-- Zoom level 8
DROP
    MATERIALIZED VIEW IF EXISTS osm_amenity_z8 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_amenity_z8 AS WITH filtered AS(
        SELECT
            tags -> 'amenity' AS tag,
            st_buffer(
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 8 )
                ),
                78270 / POWER( 2, 8 )* 1.1,
                'join=mitre'
            ) AS geom
        FROM
            osm_amenity_z9
        WHERE
            geom IS NOT NULL
            AND NOT ST_IsEmpty(geom)
            AND st_area(geom)> POWER( 78270 / POWER( 2, 8 ), 2 )* 32
    ),
    clustered AS(
        SELECT
            tag,
            geom,
            st_clusterdbscan(
                geom,
                0,
                0
            ) OVER(
                PARTITION BY tag
            ) AS cluster
        FROM
            filtered
    ) SELECT
        ROW_NUMBER() OVER() AS id,
        jsonb_build_object(
            'amenity',
            tag
        ) AS tags,
        st_simplifypreservetopology(
            (
                st_dump(
                    st_buffer(
                        st_collect(geom),
                        - 78270 / POWER( 2, 8 ),
                        'join=mitre'
                    )
                )
            ).geom,
            78270 / POWER( 2, 8 )
        ) AS geom
    FROM
        clustered
    WHERE
        geom IS NOT NULL
        AND NOT ST_IsEmpty(geom)
        AND st_area(geom)> POWER( 78270 / POWER( 2, 8 ), 2 )* 32
    GROUP BY
        tag,
        cluster WITH NO DATA;

-- Zoom level 7
DROP
    MATERIALIZED VIEW IF EXISTS osm_amenity_z7 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_amenity_z7 AS WITH filtered AS(
        SELECT
            tags -> 'amenity' AS tag,
            st_buffer(
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 7 )
                ),
                78270 / POWER( 2, 7 )* 1.1,
                'join=mitre'
            ) AS geom
        FROM
            osm_amenity_z8
        WHERE
            geom IS NOT NULL
            AND NOT ST_IsEmpty(geom)
            AND st_area(geom)> POWER( 78270 / POWER( 2, 7 ), 2 )* 32
    ),
    clustered AS(
        SELECT
            tag,
            geom,
            st_clusterdbscan(
                geom,
                0,
                0
            ) OVER(
                PARTITION BY tag
            ) AS cluster
        FROM
            filtered
    ) SELECT
        ROW_NUMBER() OVER() AS id,
        jsonb_build_object(
            'amenity',
            tag
        ) AS tags,
        st_simplifypreservetopology(
            (
                st_dump(
                    st_buffer(
                        st_collect(geom),
                        - 78270 / POWER( 2, 7 ),
                        'join=mitre'
                    )
                )
            ).geom,
            78270 / POWER( 2, 7 )
        ) AS geom
    FROM
        clustered
    WHERE
        geom IS NOT NULL
        AND NOT ST_IsEmpty(geom)
        AND st_area(geom)> POWER( 78270 / POWER( 2, 7 ), 2 )* 32
    GROUP BY
        tag,
        cluster WITH NO DATA;

-- Zoom level 6
DROP
    MATERIALIZED VIEW IF EXISTS osm_amenity_z6 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_amenity_z6 AS WITH filtered AS(
        SELECT
            tags -> 'amenity' AS tag,
            st_buffer(
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 6 )
                ),
                78270 / POWER( 2, 6 )* 1.1,
                'join=mitre'
            ) AS geom
        FROM
            osm_amenity_z7
        WHERE
            geom IS NOT NULL
            AND NOT ST_IsEmpty(geom)
            AND st_area(geom)> POWER( 78270 / POWER( 2, 6 ), 2 )* 32
    ),
    clustered AS(
        SELECT
            tag,
            geom,
            st_clusterdbscan(
                geom,
                0,
                0
            ) OVER(
                PARTITION BY tag
            ) AS cluster
        FROM
            filtered
    ) SELECT
        ROW_NUMBER() OVER() AS id,
        jsonb_build_object(
            'amenity',
            tag
        ) AS tags,
        st_simplifypreservetopology(
            (
                st_dump(
                    st_buffer(
                        st_collect(geom),
                        - 78270 / POWER( 2, 6 ),
                        'join=mitre'
                    )
                )
            ).geom,
            78270 / POWER( 2, 6 )
        ) AS geom
    FROM
        clustered
    WHERE
        geom IS NOT NULL
        AND NOT ST_IsEmpty(geom)
        AND st_area(geom)> POWER( 78270 / POWER( 2, 6 ), 2 )* 32
    GROUP BY
        tag,
        cluster WITH NO DATA;

-- Zoom level 5
DROP
    MATERIALIZED VIEW IF EXISTS osm_amenity_z5 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_amenity_z5 AS WITH filtered AS(
        SELECT
            tags -> 'amenity' AS tag,
            st_buffer(
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 5 )
                ),
                78270 / POWER( 2, 5 )* 1.1,
                'join=mitre'
            ) AS geom
        FROM
            osm_amenity_z6
        WHERE
            geom IS NOT NULL
            AND NOT ST_IsEmpty(geom)
            AND st_area(geom)> POWER( 78270 / POWER( 2, 5 ), 2 )* 32
    ),
    clustered AS(
        SELECT
            tag,
            geom,
            st_clusterdbscan(
                geom,
                0,
                0
            ) OVER(
                PARTITION BY tag
            ) AS cluster
        FROM
            filtered
    ) SELECT
        ROW_NUMBER() OVER() AS id,
        jsonb_build_object(
            'amenity',
            tag
        ) AS tags,
        st_simplifypreservetopology(
            (
                st_dump(
                    st_buffer(
                        st_collect(geom),
                        - 78270 / POWER( 2, 5 ),
                        'join=mitre'
                    )
                )
            ).geom,
            78270 / POWER( 2, 5 )
        ) AS geom
    FROM
        clustered
    WHERE
        geom IS NOT NULL
        AND NOT ST_IsEmpty(geom)
        AND st_area(geom)> POWER( 78270 / POWER( 2, 5 ), 2 )* 32
    GROUP BY
        tag,
        cluster WITH NO DATA;

-- Zoom level 4
DROP
    MATERIALIZED VIEW IF EXISTS osm_amenity_z4 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_amenity_z4 AS WITH filtered AS(
        SELECT
            tags -> 'amenity' AS tag,
            st_buffer(
                st_simplifypreservetopology(
                    geom,
                    78270 / POWER( 2, 4 )
                ),
                78270 / POWER( 2, 4 )* 1.1,
                'join=mitre'
            ) AS geom
        FROM
            osm_amenity_z6
        WHERE
            geom IS NOT NULL
            AND NOT ST_IsEmpty(geom)
            AND st_area(geom)> POWER( 78270 / POWER( 2, 4 ), 2 )* 32
    ),
    clustered AS(
        SELECT
            tag,
            geom,
            st_clusterdbscan(
                geom,
                0,
                0
            ) OVER(
                PARTITION BY tag
            ) AS cluster
        FROM
            filtered
    ) SELECT
        ROW_NUMBER() OVER() AS id,
        jsonb_build_object(
            'amenity',
            tag
        ) AS tags,
        st_simplifypreservetopology(
            (
                st_dump(
                    st_buffer(
                        st_collect(geom),
                        - 78270 / POWER( 2, 4 ),
                        'join=mitre'
                    )
                )
            ).geom,
            78270 / POWER( 2, 4 )
        ) AS geom
    FROM
        clustered
    WHERE
        geom IS NOT NULL
        AND NOT ST_IsEmpty(geom)
        AND st_area(geom)> POWER( 78270 / POWER( 2, 4 ), 2 )* 32
    GROUP BY
        tag,
        cluster WITH NO DATA;

-- Zoom level 3
DROP
    MATERIALIZED VIEW IF EXISTS osm_amenity_z3 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_amenity_z3 AS SELECT
        id,
        tags,
        st_simplifypreservetopology(
            geom,
            78270 / POWER( 2, 3 )
        ) AS geom
    FROM
        osm_amenity_z4
    WHERE
        geom IS NOT NULL
        AND NOT ST_IsEmpty(geom)
        AND st_area(geom)> POWER( 78270 / POWER( 2, 3 ), 2 )* 16 WITH NO DATA;

-- Zoom level 2
DROP
    MATERIALIZED VIEW IF EXISTS osm_amenity_z2 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_amenity_z2 AS SELECT
        id,
        tags,
        st_simplifypreservetopology(
            geom,
            78270 / POWER( 2, 2 )
        ) AS geom
    FROM
        osm_amenity_z4
    WHERE
        geom IS NOT NULL
        AND NOT ST_IsEmpty(geom)
        AND st_area(geom)> POWER( 78270 / POWER( 2, 2 ), 2 )* 16 WITH NO DATA;

-- Zoom level 1
DROP
    MATERIALIZED VIEW IF EXISTS osm_amenity_z1 CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_amenity_z1 AS SELECT
        id,
        tags,
        st_simplifypreservetopology(
            geom,
            78270 / POWER( 2, 1 )
        ) AS geom
    FROM
        osm_amenity_z4
    WHERE
        geom IS NOT NULL
        AND NOT ST_IsEmpty(geom)
        AND st_area(geom)> POWER( 78270 / POWER( 2, 1 ), 2 )* 16 WITH NO DATA;
