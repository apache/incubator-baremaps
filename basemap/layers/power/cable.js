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
    id: 'power_cable',
    type: 'line',
    filter: [
        'any',
        ['==', 'power', 'cable'],
        ['==', 'power', 'line'],
        ['==', 'power', 'minor_line'],
    ],
    source: 'baremaps',
    'source-layer': 'power',
    layout: {
        'line-cap': 'round',
        'line-join': 'round',
        visibility: 'visible',
    },
    paint: {
        'line-width': ['interpolate', ['exponential', 1.2], ['zoom'], 4, 0, 20, 4],
        'line-color': theme.powerCableLineColor,
    },
}
