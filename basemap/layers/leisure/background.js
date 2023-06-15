/**
 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License
 is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 or implied. See the License for the specific language governing permissions and limitations under
 the License.
 **/
import {asLayerObject, withSortKeys} from "../../utils/utils.js";
import theme from "../../theme.js";

let directives = [
    {
        filter: ['==', ['get', 'leisure'], 'swimming_pool'],
        'fill-color': theme.leisureBackgroundSwimmingPoolFillColor,
        'fill-outline-color': theme.leisureBackgroundSwimmingPoolFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'miniature_golf'],
        'fill-color': theme.leisureBackgroundMiniatureGolfFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'ice_rink'],
        'fill-color': theme.leisureBackgroundIceRinkFillColor,
        'fill-outline-color': theme.leisureBackgroundIceRinkFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'golf_course'],
        'fill-color': theme.leisureBackgroundGolfCourseFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'garden'],
        'fill-color': theme.leisureBackgroundGardenFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'dog_park'],
        'fill-color': theme.leisureBackgroundDogParkFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'playground'],
        'fill-color': theme.leisureBackgroundPlayGroundFillColor,
        'fill-outline-color': theme.leisureBackgroundPlayGroundFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'pitch'],
        'fill-color': theme.leisureBackgroundPitchFillColor,
        'fill-outline-color': theme.leisureBackgroundPitchFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'track'],
        'fill-color': theme.leisureBackgroundTrackFillColor,
        'fill-outline-color': theme.leisureBackgroundTrackFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'sports_centre'],
        'fill-color': theme.leisureBackgroundSportsCentreFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'stadium'],
        'fill-color': theme.leisureBackgroundStadiumFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'park'],
        'fill-color': theme.leisureBackgroundParkFillColor,
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'leisure',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'leisure',
    filter: ['==', ['geometry-type'], 'Polygon'],
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': true,
    },
});
