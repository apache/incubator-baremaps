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

let directives = [
    {
        filter: ['==', ['get', 'leisure'], 'swimming_pool'],
        'fill-color': 'rgb(170, 211, 223)',
        'fill-outline-color': 'rgb(120, 183, 202)',
    },
    {
        filter: ['==', ['get', 'leisure'], 'miniature_golf'],
        'fill-color': 'rgb(181, 226, 181)',
    },
    {
        filter: ['==', ['get', 'leisure'], 'ice_rink'],
        'fill-color': 'rgb(221, 236, 236)',
        'fill-outline-color': 'rgb(140, 220, 189)',
    },
    {
        filter: ['==', ['get', 'leisure'], 'golf_course'],
        'fill-color': 'rgb(181, 226, 181)',
    },
    {
        filter: ['==', ['get', 'leisure'], 'garden'],
        'fill-color': 'rgb(205, 235, 176)',
    },
    {
        filter: ['==', ['get', 'leisure'], 'dog_park'],
        'fill-color': 'rgb(224, 252, 227)',
    },
    {
        filter: ['==', ['get', 'leisure'], 'playground'],
        'fill-color': 'rgb(223, 252, 226)',
        'fill-outline-color': 'rgb(164, 221, 169)',
    },
    {
        filter: ['==', ['get', 'leisure'], 'pitch'],
        'fill-color': 'rgb(170, 224, 203)',
        'fill-outline-color': 'rgb(151, 212, 186)',
    },
    {
        filter: ['==', ['get', 'leisure'], 'track'],
        'fill-color': 'rgb(196, 224, 203)',
        'fill-outline-color': 'rgba(101, 206, 166, 1.0)',
    },
    {
        filter: ['==', ['get', 'leisure'], 'sports_centre'],
        'fill-color': 'rgb(223, 252, 226)',
    },
    {
        filter: ['==', ['get', 'leisure'], 'stadium'],
        'fill-color': 'rgb(223, 252, 226)',
    },
    {
        filter: ['==', ['get', 'leisure'], 'park'],
        'fill-color': 'rgb(200, 250, 204)',
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
