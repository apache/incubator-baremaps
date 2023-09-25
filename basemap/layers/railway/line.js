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


export let directives = [
    {
        'filter': [
            'all',
            ['==', ['get', 'railway'], 'rail'],
            ['!', ['has', 'service']],
        ],
        'line-color': theme.railwayLineRailLineColor,
        'road-width': 10,
    },
    {
        'filter': ['all',
            ['==', ['get', 'railway'], 'rail'],
            ['has', 'service']
        ],
        'line-color': theme.railwayLineAllRailsLineColor,
        'road-width': 6,
    },
    {
        'filter': ['==', ['get', 'railway'], 'subway'],
        'line-color': theme.railwayLineSubwayLineColor,
        'road-width': 6,
    },
    {
        'filter': ['==', ['get', 'railway'], 'tram'],
        'line-color': theme.railwayLineTramLineColor,
        'road-width': 6,
    },
    {
        'filter': ['==', ['get', 'railway'], 'preserved'],
        'line-color': theme.railwayLinePreservedLineColor,
        'road-width': 6,
    },
    {
        'filter': ['==', ['get', 'railway'], 'funicular'],
        'line-color': theme.railwayLineFunicularLineColor,
        'road-width': 6,
    },
    {
        'filter': ['==', ['get', 'railway'], 'monorail'],
        'line-color': theme.railwayLineMonorailLineColor,
        'road-width': 6,
    },
    {
        'filter': ['==', ['get', 'railway'], 'light_rail'],
        'line-color': theme.railwayLineLigthRailLineColor,
        'road-width': 6,
    },
    {
        'filter': ['==', ['get', 'railway'], 'construction'],
        'line-color': theme.railwayLineConstructionLineColor,
        'road-width': 6,
    },
    {
        'filter': ['==', ['get', 'railway'], 'abandoned'],
        'line-color': theme.railwayLineAbandonedLineColor,
        'road-width': 2,
    },
    {
        'filter': ['==', ['get', 'railway'], 'disused'],
        'line-color': theme.railwayLineisuedLineColor,
        'road-width': 2,
    },
    {
        'filter': ['==', ['get', 'railway'], 'miniature'],
        'line-color': theme.railwayLineMiniatureLineColor,
        'road-width': 2,
    },
    {
        'filter': ['==', ['get', 'railway'], 'narrow_gauge'],
        'line-color': theme.railwayLineMarrowGaugeLineColor,
        'road-width': 2,
    },
];

export default asLayerObject(withSortKeys(directives), {
    'id': 'railway_line',
    'source': 'baremaps',
    'source-layer': 'railway',
    'type': 'line',
    'filter': ['!=', ['get', 'tunnel'], 'yes'],
    'layout': {
        'visibility': 'visible',
        'line-cap': 'round',
        'line-join': 'round',
    },
});
