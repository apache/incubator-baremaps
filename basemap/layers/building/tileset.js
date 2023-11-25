/**
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to you under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **/

export default {
    id: 'building',
    queries: [
        {
            minzoom: 13,
            maxzoom: 20,
            sql: `
                SELECT
                    id,
                    tags
                        || jsonb_build_object('extrusion:base',
                            CASE
                                WHEN tags ? 'building:min_height'
                                    THEN convert_to_number(tags ->> 'building:min_height', 0)
                                WHEN tags ? 'building:min_level'
                                    THEN convert_to_number(tags ->> 'building:min_level', 0) * 3
                                ELSE 0
                            END)
                        || jsonb_build_object('extrusion:height', 
                            CASE
                                WHEN tags ? 'height'
                                    THEN convert_to_number(tags ->> 'height', 6)
                                WHEN tags ? 'building:height'
                                    THEN convert_to_number(tags ->> 'building:height', 6)
                                WHEN tags ? 'building:levels'
                                    THEN convert_to_number(tags ->> 'building:levels', 2) * 3
                                ELSE 6
                            END) as tags,
                    geom
                FROM osm_ways
                WHERE (tags ? 'building' OR tags ? 'building:part') AND ((NOT tags ? 'layer') OR convert_to_number(tags ->> 'layer', 0) >= 0)`,
        },
        {
            minzoom: 13,
            maxzoom: 20,
            sql: `
                SELECT
                    id,
                    tags
                        || jsonb_build_object('extrusion:base',
                            CASE
                                WHEN tags ? 'building:min_height'
                                    THEN convert_to_number(tags ->> 'building:min_height', 0)
                                WHEN tags ? 'building:min_level'
                                    THEN convert_to_number(tags ->> 'building:min_level', 0) * 3
                                ELSE 0
                            END)
                        || jsonb_build_object('extrusion:height',
                            CASE
                                WHEN tags ? 'height'
                                    THEN convert_to_number(tags ->> 'height', 6)
                                WHEN tags ? 'building:height'
                                    THEN convert_to_number(tags ->> 'building:height', 6)
                                WHEN tags ? 'building:levels'
                                    THEN convert_to_number(tags ->> 'building:levels', 2) * 3
                                ELSE 6
                            END) as tags,
                    geom
                FROM osm_relations
                WHERE (tags ? 'building' OR tags ? 'building:part') AND ((NOT tags ? 'layer') OR convert_to_number(tags ->> 'layer', 0) >= 0)`,
        },
    ],
}
