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
        'line-color': 'rgb(223, 55, 106)',
        'road-gap-width': 12,
        'road-width': 2,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'trunk'],
            ['==', ['get', 'highway'], 'trunk_link'],
        ],
        'line-color': 'rgb(212, 91, 54)',
        'road-gap-width': 8,
        'road-width': 2,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'primary'],
            ['==', ['get', 'highway'], 'primary_link'],
        ],
        'line-color': 'rgb(173, 132, 56)',
        'road-gap-width': 10,
        'road-width': 2,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'secondary'],
            ['==', ['get', 'highway'], 'secondary_link'],
        ],
        'line-color': 'rgb(139, 149, 60)',
        'road-gap-width': 8,
        'road-width': 2,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'tertiary'],
            ['==', ['get', 'highway'], 'tertiary_link'],
        ],
        'line-color': 'rgb(171, 170, 169)',
        'road-gap-width': 8,
        'road-width': 2,
    },
    {
        filter: ['==', ['get', 'highway'], 'unclassified'],
        'line-color': 'rgb(191, 185, 184)',
        'road-gap-width': 4,
        'road-width': 2,
    },
    {
        filter: ['==', ['get', 'highway'], 'residential'],
        'line-color': 'rgb(191, 185, 184)',
        'road-gap-width': 4,
        'road-width': 2,
    },
    {
        filter: ['==', ['get', 'highway'], 'living_street'],
        'line-color': 'rgb(186, 186, 186)',
        'road-gap-width': 4,
        'road-width': 2,
    },
    {
        filter: ['==', ['get', 'highway'], 'service'],
        'line-color': 'rgb(192, 189, 189)',
        'road-gap-width': 4,
        'road-width': 2,
    },
    {
        filter: [
            'all',
            ['==', ['get', 'highway'], 'pedestrian'],
            ['!=', ['get', '$type'], 'Polygon'],
        ],
        'line-color': 'rgb(166, 165, 163)',
        'road-gap-width': 2,
        'road-width': 2,
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'bridge_outline',
    source: 'baremaps',
    'source-layer': 'highway',
    type: 'line',
    layout: {
        visibility: 'visible',
        'line-cap': 'butt',
        'line-join': 'miter',
    },
    filter: ['any', ['==', ['get', 'bridge'], 'yes']],
});
