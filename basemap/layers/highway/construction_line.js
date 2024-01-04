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
import theme from "../../theme.js";
import {asLayerObject, withSortKeys} from "../../utils/utils.js";


let directives = [
    {
        filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['any',
                ['==', ['get', 'construction'], 'motorway'],
                ['==', ['get', 'construction'], 'motorway_link'],
            ]
        ],
        'line-color': theme.constructionLineConstructionMotorwayLineColor,
        'road-width': 16,
    },
    {
        filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['any',
                ['==', ['get', 'construction'], 'trunk'],
                ['==', ['get', 'construction'], 'trunk_link'],
            ]
        ],
        'line-color': theme.constructionLineConstructionTrunkLineColor,
        'road-width': 14,
    },
    {
        filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['any',
                ['==', ['get', 'construction'], 'primary'],
                ['==', ['get', 'construction'], 'primary_link'],
            ]
        ],
        'line-color': theme.constructionLineConstructionPrimaryLineColor,
        'road-width': 14,
    },
    {
        filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['any',
                ['==', ['get', 'construction'], 'secondary'],
                ['==', ['get', 'construction'], 'secondary_link'],
            ]
        ],
        'line-color': theme.constructionLineConstructionSecondaryLineColor,
        'road-width': 12,
    },
    {
        filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['any',
                ['==', ['get', 'construction'], 'tertiary'],
                ['==', ['get', 'construction'], 'tertiary_link'],
            ]
        ],
        'line-color': theme.constructionLineConstructionTertiaryLineColor,
        'road-width': 12,
    },
    {
        filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['==', ['get', 'construction'], 'unclassified'],
        ],
        'line-color': theme.constructionLineConstructionUnclassifiedLineColor,
        'road-width': 8,
    },
    {
        filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['==', ['get', 'construction'], 'residential'],
        ],
        'line-color': theme.constructionLineConstructionResidentialLineColor,
        'road-width': 8,
    },
    {
        filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['==', ['get', 'construction'], 'living_street'],
        ],
        'line-color': theme.constructionLineConstructionLivingStreetLineColor,
        'road-width': 8,
    },
    {
        filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['==', ['get', 'construction'], 'service'],
        ],
        'line-color': theme.constructionLineConstructionServiceLineColor,
        'road-width': 8,
    },
    {
        filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['==', ['get', 'construction'], 'raceway'],
        ],
        'line-color': theme.constructionLineConstructionRacewayLineColor,
        'road-width': 8,
    },
]

export default asLayerObject(withSortKeys(directives), {
    id: 'highway_construction_line',
    source: 'baremaps',
    'source-layer': 'highway',
    type: 'line',
    layout: {
        visibility: 'visible',
        'line-cap': 'butt',
        'line-join': 'round',
    },
    paint: {
        'line-dasharray': [1, 1],
    },
});
