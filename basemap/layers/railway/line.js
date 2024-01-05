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
        'line-width-stops': theme.railwayRailLineWidth,
    },
    {
        'filter': ['all',
            ['==', ['get', 'railway'], 'rail'],
            ['has', 'service']
        ],
        'line-color': theme.railwayLineAllRailsLineColor,
        'line-width-stops': theme.railwayServiceLineWidth,
    },
    {
        'filter': ['==', ['get', 'railway'], 'subway'],
        'line-color': theme.railwayLineSubwayLineColor,
        'line-width-stops': theme.railwaySubwayLineWidth,
    },
    {
        'filter': ['==', ['get', 'railway'], 'tram'],
        'line-color': theme.railwayLineTramLineColor,
        'line-width-stops': theme.railwayTramLineWidth,
    },
    {
        'filter': ['==', ['get', 'railway'], 'preserved'],
        'line-color': theme.railwayLinePreservedLineColor,
        'line-width-stops': theme.railwayPreservedLineWidth,
    },
    {
        'filter': ['==', ['get', 'railway'], 'funicular'],
        'line-color': theme.railwayLineFunicularLineColor,
        'line-width-stops': theme.railwayFunicularLineWidth,
    },
    {
        'filter': ['==', ['get', 'railway'], 'monorail'],
        'line-color': theme.railwayLineMonorailLineColor,
        'line-width-stops': theme.railwayMonorailLineWidth,
    },
    {
        'filter': ['==', ['get', 'railway'], 'light_rail'],
        'line-color': theme.railwayLineLigthRailLineColor,
        'line-width-stops': theme.railwayLightRailLineWidth,
    },
    {
        'filter': ['==', ['get', 'railway'], 'construction'],
        'line-color': theme.railwayLineConstructionLineColor,
        'line-width-stops': theme.railwayConstructionLineWidth,
    },
    {
        'filter': ['==', ['get', 'railway'], 'abandoned'],
        'line-color': theme.railwayLineAbandonedLineColor,
        'line-width-stops': theme.railwayAbandonedLineWidth,
    },
    {
        'filter': ['==', ['get', 'railway'], 'disused'],
        'line-color': theme.railwayLineisuedLineColor,
        'line-width-stops': theme.railwayDisusedLineWidth,
    },
    {
        'filter': ['==', ['get', 'railway'], 'miniature'],
        'line-color': theme.railwayLineMiniatureLineColor,
        'line-width-stops': theme.railwayMiniatureLineWidth,
    },
    {
        'filter': ['==', ['get', 'railway'], 'narrow_gauge'],
        'line-color': theme.railwayLineMarrowGaugeLineColor,
        'line-width-stops': theme.railwayNarrowGaugeLineWidth,
    },
];

export default asLayerObject(withSortKeys(directives), {
    'id': 'railway_line',
    'source': 'baremaps',
    'source-layer': 'railway',
    'type': 'line',
    'layout': {
        'visibility': 'visible',
        'line-cap': 'round',
        'line-join': 'round',
    },
    'filter': ['all',
        ['==', ['geometry-type'], 'LineString'],
        ['!=', ['get', 'tunnel'], 'yes'],
    ],
});
