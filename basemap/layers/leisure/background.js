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
        'fill-color': theme.directivesSwimmingPoolFillColor,
        'fill-outline-color': theme.directivesSwimmingPoolFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'miniature_golf'],
        'fill-color': theme.directivesMiniatureGolfFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'ice_rink'],
        'fill-color': theme.directivesIceRinkFillColor,
        'fill-outline-color': theme.directivesIceRinkFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'golf_course'],
        'fill-color': theme.directivesGolfCourseFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'garden'],
        'fill-color': theme.directivesGardenFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'dog_park'],
        'fill-color': theme.directivesDogParkFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'playground'],
        'fill-color': theme.directivesPlayGroundFillColor,
        'fill-outline-color': theme.directivesPlayGroundFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'pitch'],
        'fill-color': theme.directivesPitchFillColor,
        'fill-outline-color': theme.directivesPitchFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'track'],
        'fill-color': theme.directivesTrackFillColor,
        'fill-outline-color': theme.directivesTrackFillOutlineColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'sports_centre'],
        'fill-color': theme.directivesSportsCentreFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'stadium'],
        'fill-color': theme.directivesStadiumFillColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'park'],
        'fill-color': theme.directivesParkFillColor,
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
