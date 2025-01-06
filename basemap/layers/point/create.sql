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
    OR REPLACE VIEW osm_point AS SELECT
        id,
        jsonb_build_object(
            'amenity',
            tags -> 'amenity',
            'leisure',
            tags -> 'leisure',
            'tourism',
            tags -> 'tourism',
            'historic',
            tags -> 'historic',
            'man_made',
            tags -> 'man_made',
            'shop',
            tags -> 'shop',
            'sport',
            tags -> 'sport',
            'natural',
            tags -> 'natural',
            'waterway',
            tags -> 'waterway',
            'power',
            tags -> 'power',
            'office',
            tags -> 'office',
            'diplomatic',
            tags -> 'diplomatic',
            'religion',
            tags -> 'religion',
            'place',
            tags -> 'place',
            'capital',
            tags -> 'capital',
            'barrier',
            tags -> 'barrier',
            'highway',
            tags -> 'highway',
            'railway',
            tags -> 'railway',
            'emergency',
            tags -> 'emergency',
            'aeroway',
            tags -> 'aeroway',
            'military',
            tags -> 'military',
            'generator:source',
            tags -> 'generator:source',
            'generator:method',
            tags -> 'generator:method',
            'tower:type',
            tags -> 'tower:type',
            'tower:construction',
            tags -> 'tower:construction',
            'castle_type',
            tags -> 'castle_type',
            'artwork_type',
            tags -> 'artwork_type',
            'memorial',
            tags -> 'memorial',
            'diplomatic',
            tags -> 'diplomatic',
            'name',
            tags -> 'name',
            'population',
            tags -> 'population'
        ) AS tags,
        geom
    FROM
        osm_node
    WHERE
        geom IS NOT NULL
        AND tags != '{}';

CREATE
    OR REPLACE VIEW osm_point_z20 AS SELECT
        *
    FROM
        osm_point;

CREATE
    OR REPLACE VIEW osm_point_z19 AS SELECT
        *
    FROM
        osm_point;

CREATE
    OR REPLACE VIEW osm_point_z18 AS SELECT
        *
    FROM
        osm_point;

CREATE
    OR REPLACE VIEW osm_point_z17 AS SELECT
        *
    FROM
        osm_point;

CREATE
    OR REPLACE VIEW osm_point_z16 AS SELECT
        *
    FROM
        osm_point;

CREATE
    OR REPLACE VIEW osm_point_z15 AS SELECT
        *
    FROM
        osm_point;

CREATE
    OR REPLACE VIEW osm_point_z14 AS SELECT
        *
    FROM
        osm_point;

DROP
    MATERIALIZED VIEW IF EXISTS osm_point_z13;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_point_z13 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_point
    WHERE
        (
            tags ->> 'place'
        )= ANY(
            ARRAY [ 'region',
            'province',
            'district',
            'county',
            'municipality',
            'city',
            'town',
            'village',
            'quarter',
            'hamlet' ]
        )
        OR(
            tags ->> 'natural'
        )= ANY(
            ARRAY [ 'peak',
            'volcano',
            'spring' ]
        )
        OR(
            tags ->> 'highway'
        )= 'motorway_junction'
        OR(
            tags ->> 'tourism'
        )= 'wilderness_hut'
        OR(
            tags ->> 'waterway'
        )= 'waterfall'
        OR(
            tags ->> 'railway'
        )= 'level_crossing' WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_point_z12;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_point_z12 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_point
    WHERE
        (
            tags ->> 'place'
        )= ANY(
            ARRAY [ 'region',
            'province',
            'district',
            'county',
            'municipality',
            'city',
            'town',
            'village' ]
        )
        OR(
            tags ->> 'natural'
        )= ANY(
            ARRAY [ 'peak',
            'volcano' ]
        )
        OR(
            tags ->> 'highway'
        )= 'motorway_junction'
        OR(
            tags ->> 'tourism'
        )= 'wilderness_hut'
        OR(
            tags ->> 'waterway'
        )= 'waterfall' WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_point_z11;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_point_z11 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_point
    WHERE
        (
            tags ->> 'place'
        )= ANY(
            ARRAY [ 'country',
            'state',
            'region',
            'province',
            'district',
            'county',
            'municipality',
            'city',
            'town',
            'village' ]
        )
        OR(
            tags ->> 'natural'
        )= ANY(
            ARRAY [ 'peak',
            'volcano' ]
        )
        OR(
            tags ->> 'highway'
        )= 'motorway_junction' WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_point_z10;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_point_z10 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_point
    WHERE
        (
            tags ->> 'place'
        )= ANY(
            ARRAY [ 'country',
            'state',
            'region',
            'province',
            'district',
            'county',
            'municipality',
            'city',
            'town' ]
        )
        OR(
            tags ->> 'natural'
        )= ANY(
            ARRAY [ 'peak',
            'volcano' ]
        )
        OR(
            tags ->> 'highway'
        )= 'motorway_junction' WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_point_z9;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_point_z9 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_point
    WHERE
        (
            tags ->> 'place'
        )= ANY(
            ARRAY [ 'country',
            'state',
            'region',
            'province',
            'district',
            'county',
            'municipality',
            'city',
            'town' ]
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_point_z8;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_point_z8 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_point
    WHERE
        (
            tags ->> 'place'
        )= ANY(
            ARRAY [ 'country',
            'state',
            'region',
            'province',
            'district',
            'county',
            'municipality',
            'city',
            'town' ]
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_point_z7;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_point_z7 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_point
    WHERE
        (
            tags ->> 'place'
        )= ANY(
            ARRAY [ 'country',
            'city',
            'sea',
            'state',
            'county' ]
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_point_z6;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_point_z6 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_point
    WHERE
        (
            tags ->> 'place'
        )= ANY(
            ARRAY [ 'country',
            'city',
            'sea',
            'state',
            'county' ]
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_point_z5;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_point_z5 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_point
    WHERE
        (
            tags ->> 'place'
        )= ANY(
            ARRAY [ 'country',
            'city',
            'sea',
            'state',
            'county' ]
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_point_z4;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_point_z4 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_point
    WHERE
        (
            tags ->> 'place'
        )= ANY(
            ARRAY [ 'country',
            'city',
            'sea' ]
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_point_z3;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_point_z3 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_point
    WHERE
        (
            tags ->> 'place'
        )= ANY(
            ARRAY [ 'country',
            'city',
            'sea' ]
        ) WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_point_z2;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_point_z2 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_point
    WHERE
        (
            tags ->> 'place'
        )= 'country' WITH NO DATA;

DROP
    MATERIALIZED VIEW IF EXISTS osm_point_z1;

CREATE
    MATERIALIZED VIEW IF NOT EXISTS osm_point_z1 AS SELECT
        id,
        tags,
        geom
    FROM
        osm_point
    WHERE
        (
            tags ->> 'place'
        )= 'country' WITH NO DATA;
