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

export default {
    id: 'building_number',
    type: 'symbol',
    source: 'baremaps',
    'source-layer': 'building',
    layout: {
        'text-allow-overlap': false,
        'text-anchor': 'center',
        'text-field': '{addr:housenumber}',
        'text-font': ['Noto Sans Regular'],
        'text-offset': [0, 0],
        'text-size': [
            'interpolate',
            ['exponential', 1],
            ['zoom'],
            15,
            0,
            16,
            11,
            20,
            11,
        ],
        visibility: 'visible',
    },
    paint: {
        'text-color': theme.buildingNumberTextColor,
        'text-halo-color': theme.buildingNumberTextHaloColor,
        'text-halo-width': 1.2,
    },
}
