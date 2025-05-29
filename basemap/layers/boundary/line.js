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
        filter: ['==', ['get', 'admin_level'], "1"],
        'line-color': theme.boundaryAdminLevelLineColor,
        'line-width': 3,
    },
    {
        filter: ['==', ['get', 'admin_level'], "2"],
        'line-color': theme.boundaryAdminLevelLineColor,
        'line-width': 3,
    },
    {
        filter: ['==', ['get', 'admin_level'], "3"],
        'line-color': theme.boundaryAdminLevelLineColor,
        'line-width': 2,
    },
    {
        filter: ['==', ['get', 'admin_level'], "4"],
        'line-color': theme.boundaryAdminLevelLineColor,
        'line-width': 2,
    },
    {
        filter: ['has', 'boundary'],
        'line-color': theme.boundaryAdminLevelLineColor,
        'line-width': 1,
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'boundary',
    type: 'line',
    source: 'baremaps',
    'source-layer': 'boundary',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'line-width': [
            'interpolate',
            ['exponential', 1],
            ['zoom'],
            0, 0.5,
            10, [
                'case',
                ['==', ['get', 'maritime'], 'yes'],
                2, // If maritime is 'yes'
                [
                    'case',
                    ['==', ['get', 'admin_level'], '4'],
                    1, // If admin_level is '4'
                    3 // For all other cases
                ]
            ],
            20, 5
        ]
    },
    filter: ['==', ["geometry-type"], 'LineString'],
});
