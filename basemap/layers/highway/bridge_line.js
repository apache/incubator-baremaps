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
        'line-color': theme.bridgeLineMotorwayLineColor,
        'line-width-stops': theme.highwayMotorwayLineWidth,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'trunk'],
            ['==', ['get', 'highway'], 'trunk_link'],
        ],
        'line-color': theme.bridgeLineTrunkLineColor,
        'line-width-stops': theme.highwayTrunkLineWidth,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'primary'],
            ['==', ['get', 'highway'], 'primary_link'],
        ],
        'line-color': theme.bridgeLinePrimaryLineColor,
        'line-width-stops': theme.highwayPrimaryLineWidth,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'secondary'],
            ['==', ['get', 'highway'], 'secondary_link'],
        ],
        'line-color': theme.bridgeLineSecondaryLineColor,
        'line-width-stops': theme.highwaySecondaryLineWidth,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'tertiary'],
            ['==', ['get', 'highway'], 'tertiary_link'],
        ],
        'line-color': theme.bridgeLineTertiaryLineColor,
        'line-width-stops': theme.highwayTertiaryLineWidth,
    },
    {
        filter: ['==', ['get', 'highway'], 'unclassified'],
        'line-color': theme.bridgeLineUnclassifiedLineColor,
        'line-width-stops': theme.highwayUnclassifiedLineWidth,
    },
    {
        filter: ['==', ['get', 'highway'], 'residential'],
        'line-color': theme.bridgeLineResidentialLineColor,
        'line-width-stops': theme.highwayResidentialLineWidth,
    },
    {
        filter: ['==', ['get', 'highway'], 'living_street'],
        'line-color': theme.bridgeLineLivingStreetLineColor,
        'line-width-stops': theme.highwayLivingStreetLineWidth,
    },
    {
        filter: ['==', ['get', 'highway'], 'service'],
        'line-color': theme.bridgeLineServiceLineColor,
        'line-width-stops': theme.highwayServiceLineWidth,
    },
    {
        filter: ['==', ['get', 'highway'], 'track'],
        'line-color': theme.bridgeLineTrackLineColor,
        'line-width-stops': theme.highwayMinorLineWidth,
    },
    {
        filter: ['==', ['get', 'highway'], 'raceway'],
        'line-color': theme.bridgeLineRacewayLineColor,
        'line-width-stops': theme.highwayRacewayLineWidth,
    },
    {
        filter: [
            'all',
            ['==', ['get', 'highway'], 'pedestrian'],
            ['!=', ['geometry-type'], 'Polygon'],
        ],
        'line-color': theme.bridgeLinePedestrianLineColor,
        'line-width-stops': theme.highwayPedestrianLineWidth,
    },
]

export default asLayerObject(withSortKeys(directives), {
    id: 'bridge_line',
    source: 'baremaps',
    'source-layer': 'highway',
    type: 'line',
    layout: {
        visibility: 'visible',
        'line-cap': 'butt',
        'line-join': 'miter',
    },
    filter: ['any', ['==', ['get', 'bridge'], 'yes']],
});
