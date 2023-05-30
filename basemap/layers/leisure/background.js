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
import colorScheme from "../../colorScheme.js";

let directives = [
    {
        filter: ['==', ['get', 'leisure'], 'swimming_pool'],
        'fill-color': colorScheme.directivesSwimmingPoolFillColor,
        'fill-outline-color': colorScheme.directivesSwimmingPoolFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'miniature_golf'],
        'fill-color': colorScheme.directivesMiniatureGolfFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'ice_rink'],
        'fill-color': colorScheme.directivesIceRinkFillColor,
        'fill-outline-color': colorScheme.directivesIceRinkFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'golf_course'],
        'fill-color': colorScheme.directivesGolfCourseFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'garden'],
        'fill-color': colorScheme.directivesGardenFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'dog_park'],
        'fill-color': colorScheme.directivesDogParkFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'playground'],
        'fill-color': colorScheme.directivesPlayGroundFillColor,
        'fill-outline-color': colorScheme.directivesPlayGroundFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'pitch'],
        'fill-color': colorScheme.directivesPitchFillColor,
        'fill-outline-color': colorScheme.directivesPitchFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'track'],
        'fill-color': colorScheme.directivesTrackFillColor,
        'fill-outline-color': colorScheme.directivesTrackFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'sports_centre'],
        'fill-color': colorScheme.directivesSportsCentreFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'stadium'],
        'fill-color': colorScheme.directivesStadiumFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'park'],
        'fill-color': colorScheme.directivesParkFillColor,
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
