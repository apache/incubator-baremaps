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
        filter: ['==', ['get', 'leisure'], 'marina'],
        'fill-color': theme.leisureOverlayMarinaFillColor,
        'fill-outline-color': theme.leisureOverlayMarinaFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'swimming_pool'],
        'fill-color': theme.leisureBackgroundSwimmingPoolFillColor,
        'fill-outline-color': theme.leisureOverlaySwimmingPoolFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'fitness_station'],
        'fill-color': theme.leisureOverlayFitnessStationFillColor,
        'fill-outline-color': theme.leisureOverlayFitnessStationFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'miniature_golf'],
        'fill-color': theme.leisureOverlayMiniatureGolfFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'ice_rink'],
        'fill-color': theme.leisureOverlayIceRinkFillColor,
        'fill-outline-color': theme.leisureOverlayIceRinkFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'dog_park'],
        'fill-color': theme.leisureOverlayDogParkFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'playground'],
        'fill-color': theme.leisureOverlayPlayGroundFillColor,
        'fill-outline-color': theme.leisureOverlayPlayGroundFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'pitch'],
        'fill-color': theme.leisureOverlayPitchFillColor,
        'fill-outline-color': theme.leisureOverlayPitchFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'track'],
        'fill-color': theme.leisureBackgroundTrackFillColor,
        'fill-outline-color': theme.leisureBackgroundTrackFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'stadium'],
        'fill-color': theme.leisureOverlayStadiumFillColor,
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'leisure_overlay',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'leisure',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': false,
    },
});
