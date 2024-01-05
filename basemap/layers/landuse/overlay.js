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

import {asLayerObject, withFillSortKey, withSortKeys} from "../../utils/utils.js";
import theme from "../../theme.js";

let directives = [
    {
        filter: ['==', ['get', 'landuse'], 'forest'],
        'fill-color': theme.landuseOverlayForestFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'grass'],
        'fill-color': theme.landuseOverlayGrassFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'greenhouse_horticulture'],
        'fill-color': theme.landuseOverlayGreenhouseHorticultureFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'orchard'],
        'fill-color': theme.landuseOverlayOrchardFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'meadow'],
        'fill-color': theme.landuseOverlayMeadowFillColor,
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'landuse_overlay',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'landuse',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': false,
    },
    filter: ['==', ['geometry-type'], 'Polygon'],
});
