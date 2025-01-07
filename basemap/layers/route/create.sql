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
CREATE
    OR REPLACE VIEW osm_route AS SELECT
        id,
        tags,
        geom
    FROM
        osm_way
    WHERE
        geom IS NOT NULL
        AND tags ? 'route';

CREATE
    OR REPLACE VIEW osm_route_z20 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_route;

CREATE
    OR REPLACE VIEW osm_route_z19 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_route;

CREATE
    OR REPLACE VIEW osm_route_z18 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_route;

CREATE
    OR REPLACE VIEW osm_route_z17 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_route;

CREATE
    OR REPLACE VIEW osm_route_z16 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_route;

CREATE
    OR REPLACE VIEW osm_route_z15 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_route;

CREATE
    OR REPLACE VIEW osm_route_z14 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_route;

CREATE
    OR REPLACE VIEW osm_route_z13 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_route;

DROP
    MATERIALIZED VIEW IF EXISTS osm_route_simplified CASCADE;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_route_simplified AS SELECT
        id,
        tags,
        geom
    FROM
        (
            SELECT
                MIN( id ) AS id,
                jsonb_build_object(
                    'route',
                    tags -> 'route'
                ) AS tags,
                (
                    st_dump(
                        st_linemerge(
                            st_collect(geom)
                        )
                    )
                ).geom AS geom
            FROM
                osm_way
            WHERE
                tags ->> 'route' IN(
                    'light_rail',
                    'monorail',
                    'rail',
                    'subway',
                    'tram'
                )
                AND NOT tags ? 'service'
            GROUP BY
                tags -> 'route'
        ) AS mergedDirective WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_route_z12;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_route_z12 AS SELECT
        id,
        tags,
        st_simplifypreservetopology(
            geom,
            78270 / POWER( 2, 12 )
        ) AS geom
    FROM
        osm_route_simplified
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 12 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_route_z11;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_route_z11 AS SELECT
        id,
        tags,
        st_simplifypreservetopology(
            geom,
            78270 / POWER( 2, 11 )
        ) AS geom
    FROM
        osm_route_simplified
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 11 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_route_z10;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_route_z10 AS SELECT
        id,
        tags,
        st_simplifypreservetopology(
            geom,
            78270 / POWER( 2, 10 )
        ) AS geom
    FROM
        osm_route_simplified
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 10 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_route_z9;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_route_z9 AS SELECT
        id,
        tags,
        st_simplifypreservetopology(
            geom,
            78270 / POWER( 2, 9 )
        ) AS geom
    FROM
        osm_route_simplified
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 9 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_route_z8;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_route_z8 AS SELECT
        id,
        tags,
        st_simplifypreservetopology(
            geom,
            78270 / POWER( 2, 8 )
        ) AS geom
    FROM
        osm_route_simplified
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 8 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_route_z7;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_route_z7 AS SELECT
        id,
        tags,
        st_simplifypreservetopology(
            geom,
            78270 / POWER( 2, 7 )
        ) AS geom
    FROM
        osm_route_simplified
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 7 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_route_z6;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_route_z6 AS SELECT
        id,
        tags,
        st_simplifypreservetopology(
            geom,
            78270 / POWER( 2, 6 )
        ) AS geom
    FROM
        osm_route_simplified
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 6 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_route_z5;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_route_z5 AS SELECT
        id,
        tags,
        st_simplifypreservetopology(
            geom,
            78270 / POWER( 2, 5 )
        ) AS geom
    FROM
        osm_route_simplified
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 5 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_route_z4;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_route_z4 AS SELECT
        id,
        tags,
        st_simplifypreservetopology(
            geom,
            78270 / POWER( 2, 4 )
        ) AS geom
    FROM
        osm_route_simplified
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 4 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_route_z3;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_route_z3 AS SELECT
        id,
        tags,
        st_simplifypreservetopology(
            geom,
            78270 / POWER( 2, 3 )
        ) AS geom
    FROM
        osm_route_simplified
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 3 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_route_z2;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_route_z2 AS SELECT
        id,
        tags,
        st_simplifypreservetopology(
            geom,
            78270 / POWER( 2, 2 )
        ) AS geom
    FROM
        osm_route_simplified
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 2 )), 2 )
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_route_z1;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_route_z1 AS SELECT
        id,
        tags,
        st_simplifypreservetopology(
            geom,
            78270 / POWER( 2, 1 )
        ) AS geom
    FROM
        osm_route_simplified
    WHERE
        geom IS NOT NULL
        AND(
            st_area(
                st_envelope(geom)
            )> POWER(( 78270 / POWER( 2, 1 )), 2 )
        ) WITH NO DATA;
