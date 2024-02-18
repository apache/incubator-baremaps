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
        filter: ['==', ['get', 'natural'], 'glacier'],
        'fill-color': theme.naturalGlacierBackgroundFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'wood'],
        'fill-color': theme.naturalWoodBackgroundFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'scrub'],
        'fill-color': theme.naturalScrubOverlayFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'heath'],
        'fill-color': theme.naturalHeathBackgroundFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'grassland'],
        'fill-color': theme.naturalGrasslandBackgroundFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'bare_rock'],
        'fill-color': theme.naturalBareRockBackgroundFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'scree'],
        'fill-color': theme.naturalScreeBackgroundFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'shingle'],
        'fill-color': theme.naturalShingleBackgroundFillColor
    },
    {
        filter: [
            'all',
            ['==', ['get', 'natural'], 'water'],
            ['==', ['get', 'water'], 'lake'],
        ],
        'fill-color': theme.naturalWaterBackgroundFillColor,
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'natural',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'natural',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': false,
    },
    filter: ['==', ['geometry-type'], 'Polygon'],
});
