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
    id: 'building-extrusion',
    type: 'fill-extrusion',
    source: 'baremaps',
    'source-layer': 'building',
    filter: ['!=', ['get', 'building'], 'no'],
    layout: {
        visibility: 'visible',
    },
    minzoom: 15,
    paint: {
        "fill-extrusion-base": [
            'interpolate',
            ['linear'],
            ['zoom'],
            15,
            0,
            16,
            ['get', "extrusion:base"]
        ],
        "fill-extrusion-height": [
            'interpolate',
            ['linear'],
            ['zoom'],
            15,
            0,
            16,
            ['get', "extrusion:height"]
        ],
        "fill-extrusion-opacity": [
            'interpolate',
            ['linear'],
            ['zoom'],
            15,
            0,
            16,
            0.8
        ],
        "fill-extrusion-color": theme.buildingShapeFillColor,
        // Having muliple colors for building parts results in z-fighting
        // https://github.com/maplibre/maplibre-gl-js/issues/3157
        // https://github.com/maplibre/maplibre-gl-js/issues/3187
        // "fill-extrusion-color": [
        //     "case",
        //     ["has", "building:colour"],
        //     ["get", "building:colour"],
        //     theme.buildingShapeFillColor,
        // ],
    },
}
