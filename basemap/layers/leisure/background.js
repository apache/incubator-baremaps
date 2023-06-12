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
import colorScheme from "../../theme.js";

let directives = [
    {
        filter: ['==', ['get', 'leisure'], 'swimming_pool'],
        'fill-color': colorScheme.LeisureBackgroundSwimmingPoolFillColor,
        'fill-outline-color': colorScheme.LeisureBackgroundSwimmingPoolFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'miniature_golf'],
        'fill-color': colorScheme.LeisureBackgroundMiniatureGolfFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'ice_rink'],
        'fill-color': colorScheme.LeisureBackgroundIceRinkFillColor,
        'fill-outline-color': colorScheme.LeisureBackgroundIceRinkFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'golf_course'],
        'fill-color': colorScheme.LeisureBackgroundGolfCourseFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'garden'],
        'fill-color': colorScheme.LeisureBackgroundGardenFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'dog_park'],
        'fill-color': colorScheme.LeisureBackgroundDogParkFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'playground'],
        'fill-color': colorScheme.LeisureBackgroundPlayGroundFillColor,
        'fill-outline-color': colorScheme.LeisureBackgroundPlayGroundFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'pitch'],
        'fill-color': colorScheme.LeisureBackgroundPitchFillColor,
        'fill-outline-color': colorScheme.LeisureBackgroundPitchFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'track'],
        'fill-color': colorScheme.LeisureBackgroundTrackFillColor,
        'fill-outline-color': colorScheme.LeisureBackgroundTrackFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'sports_centre'],
        'fill-color': colorScheme.LeisureBackgroundSportsCentreFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'stadium'],
        'fill-color': colorScheme.LeisureBackgroundStadiumFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'park'],
        'fill-color': colorScheme.LeisureBackgroundParkFillColor,
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
