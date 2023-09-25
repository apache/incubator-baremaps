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
    id: 'highway_label',
    type: 'symbol',
    source: 'baremaps',
    'source-layer': 'highway',
    layout: {
        'symbol-placement': 'line',
        'text-anchor': 'center',
        'text-field': ['get', 'name'],
        'text-font': ['Noto Sans Regular'],
        'text-size': ['interpolate', ['exponential', 1], ['zoom'], 13, 10, 14, 12],
        visibility: 'visible',
    },
    paint: {
        'text-color': theme.highwayLabelPaintTextColor,
        'text-halo-color': theme.highwayLabelPaintTextHaloColor,
        'text-halo-width': 1.2,
    },
}
