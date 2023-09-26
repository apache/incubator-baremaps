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

export default [
    {
        id: 'water_slide_casing',
        type: 'line',
        filter: [
            'all',
            ['==', ['get', 'attraction'], 'water_slide'],
            ['>=', ['zoom'], 15],
        ],
        source: 'baremaps',
        'source-layer': 'attraction',
        paint: {
            'line-width': ['interpolate', ['exponential', 1], ['zoom'], 16, 2, 20, 8],
        },
    },
    {
        id: 'water_slide',
        type: 'line',
        filter: [
            'all',
            ['==', ['get', 'attraction'], 'water_slide'],
            ['>=', ['zoom'], 16],
        ],
        source: 'baremaps',
        'source-layer': 'attraction',
        paint: {
            'line-color': theme.attractionStyleWaterSlideLineColor,
            'line-width': [
                'interpolate',
                ['exponential', 1],
                ['zoom'],
                15,
                0.5,
                16,
                1,
                20,
                6,
            ],
        },
    },
]
