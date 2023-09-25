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
import {asLayerObject, withSortKeys} from "../../utils/utils.js";
import theme from "../../theme.js";

let directives = [
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'motorway'],
            ['==', ['get', 'highway'], 'motorway_link'],
        ],
        'line-color': theme.highwayLineMotorwayLineColor,
        'road-width': 12,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'trunk'],
            ['==', ['get', 'highway'], 'trunk_link'],
        ],
        'line-color': theme.highwayLineTrunkLineColor,
        'road-width': 8,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'primary'],
            ['==', ['get', 'highway'], 'primary_link'],
        ],
        'line-color': theme.highwayLinePrimaryLineColor,
        'road-width': 10,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'secondary'],
            ['==', ['get', 'highway'], 'secondary_link'],
        ],
        'line-color': theme.highwayLineSecondaryLineColor,
        'road-width': 8,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'tertiary'],
            ['==', ['get', 'highway'], 'tertiary_link'],
        ],
        'line-color': theme.highwayLineTertiaryLineColor,
        'road-width': 8,
    },
    {
        filter: ['==', ['get', 'highway'], 'busway'],
        'line-color': theme.highwayLineBuswayLineColor,
        'road-width': 8,
    },
    {
        filter: ['==', ['get', 'highway'], 'unclassified'],
        'line-color': theme.highwayLineUnclassifiedLineColor,
        'road-width': 4,
    },
    {
        filter: ['==', ['get', 'highway'], 'residential'],
        'line-color': theme.highwayLineResidentialLineColor,
        'road-width': 4,
    },
    {
        filter: ['==', ['get', 'highway'], 'living_street'],
        'line-color': theme.highwayLineLivingStreetLineColor,
        'road-width': 4,
    },
    {
        filter: ['==', ['get', 'highway'], 'service'],
        'line-color': theme.highwayLineServiceLineColor,
        'road-width': 4,
    },
    {
        filter: ['==', ['get', 'highway'], 'raceway'],
        'line-color': theme.highwayLineRacewayLineColor,
        'road-width': 4,
    },
    {
        filter: [
            'all',
            ['==', ['get', 'highway'], 'pedestrian'],
            ['!=', ['get', 'area'], 'yes'],
        ],
        'line-color': theme.highwayLinePedestrianLineColor,
        'road-width': 2,
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'highway_line',
    source: 'baremaps',
    'source-layer': 'highway',
    type: 'line',
    layout: {
        visibility: 'visible',
        'line-cap': 'round',
        'line-join': 'round',
    },
    filter: [
        'all',
        ['!=', ['get', 'bridge'], 'yes'],
        ['!=', ['get', 'tunnel'], 'yes'],
        ['!=', ['get', 'layer'], '-1'],
        ['!=', ['get', 'covered'], 'yes'],
    ],
});
