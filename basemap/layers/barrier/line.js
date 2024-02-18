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
        filter: ['==', ['get', 'barrier'], 'hedge'],
        'line-color': theme.barrierHedgeLineColor,
        'line-width-stops': theme.barrierHedgeLineWidth,
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'barrier_line',
    source: 'baremaps',
    'source-layer': 'barrier',
    type: 'line',
    layout: {
        visibility: 'visible',
        'line-cap': 'round',
        'line-join': 'round',
    },
    filter: [
        'all',
        ['==', ['geometry-type'], 'LineString'],
    ],
});
