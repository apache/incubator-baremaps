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
        filter: [
            'any',
            ['==', ['get', 'highway'], 'motorway'],
            ['==', ['get', 'highway'], 'motorway_link'],
        ],
        'line-color': 'rgba(227, 82, 126, 1)',
        'road-gap-width': 12,
        'road-width': 2,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'trunk'],
            ['==', ['get', 'highway'], 'trunk_link'],
        ],
        'line-color': 'rgba(217, 111, 78, 1)',
        'road-gap-width': 8,
        'road-width': 2,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'primary'],
            ['==', ['get', 'highway'], 'primary_link'],
        ],
        'line-color': 'rgba(192, 147, 62, 1)',
        'road-gap-width': 10,
        'road-width': 2,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'secondary'],
            ['==', ['get', 'highway'], 'secondary_link'],
        ],
        'line-color': 'rgba(154, 166, 67, 1)',
        'road-gap-width': 8,
        'road-width': 2,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'tertiary'],
            ['==', ['get', 'highway'], 'tertiary_link'],
        ],
        'line-color': 'rgba(190, 189, 188, 1)',
        'road-gap-width': 8,
        'road-width': 2,
    },
    {
        filter: ['==', ['get', 'highway'], 'unclassified'],
        'line-color': 'rgba(211, 207, 206, 1)',
        'road-gap-width': 4,
        'road-width': 2,
    },
    {
        filter: ['==', ['get', 'highway'], 'residential'],
        'line-color': 'rgba(211, 207, 206, 1)',
        'road-gap-width': 4,
        'road-width': 2,
    },
    {
        filter: ['==', ['get', 'highway'], 'living_street'],
        'line-color': 'rgba(207, 207, 207, 1)',
        'road-gap-width': 4,
        'road-width': 2,
    },
    {
        filter: ['==', ['get', 'highway'], 'service'],
        'line-color': 'rgba(213, 211, 211, 1)',
        'road-gap-width': 4,
        'road-width': 2,
    },
    {
        filter: [
            'all',
            ['==', ['get', 'highway'], 'pedestrian'],
            ['!=', ['get', '$type'], 'Polygon'],
        ],
        'line-color': 'rgba(184, 183, 182, 1)',
        'road-gap-width': 2,
        'road-width': 2,
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'tunnel_outline',
    source: 'baremaps',
    'source-layer': 'highway',
    type: 'line',
    layout: {
        visibility: 'visible',
        'line-cap': 'square',
        'line-join': 'miter',
    },
    filter: [
        'any',
        ['==', ['get', 'tunnel'], 'yes'],
        ['==', ['get', 'layer'], '-1'],
        ['==', ['get', 'covered'], 'yes'],
    ],
    paint: {
        'line-dasharray': [1, 1],
    },
});
