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
        'line-color': 'rgb(233, 144, 161)',
        'road-width': 12,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'trunk'],
            ['==', ['get', 'highway'], 'trunk_link'],
        ],
        'line-color': 'rgb(250, 193, 172)',
        'road-width': 8,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'primary'],
            ['==', ['get', 'highway'], 'primary_link'],
        ],
        'line-color': 'rgb(253, 221, 179)',
        'road-width': 10,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'secondary'],
            ['==', ['get', 'highway'], 'secondary_link'],
        ],
        'line-color': 'rgb(248, 250, 202)',
        'road-width': 8,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'tertiary'],
            ['==', ['get', 'highway'], 'tertiary_link'],
        ],
        'line-color': 'rgb(254, 254, 254)',
        'road-width': 8,
    },
    {
        filter: ['==', ['get', 'highway'], 'unclassified'],
        'line-color': 'rgb(254, 254, 254)',
        'road-width': 4,
    },
    {
        filter: ['==', ['get', 'highway'], 'residential'],
        'line-color': 'rgb(254, 254, 254)',
        'road-width': 4,
    },
    {
        filter: ['==', ['get', 'highway'], 'living_street'],
        'line-color': 'rgb(237, 237, 237)',
        'road-width': 4,
    },
    {
        filter: ['==', ['get', 'highway'], 'service'],
        'line-color': 'rgb(254, 254, 254)',
        'road-width': 4,
    },
    {
        filter: ['==', ['get', 'highway'], 'raceway'],
        'line-color': 'rgb(255, 192, 203)',
        'road-width': 4,
    },
    {
        filter: [
            'all',
            ['==', ['get', 'highway'], 'pedestrian'],
            ['!=', ['get', 'area'], 'yes'],
        ],
        'line-color': 'rgb(221, 221, 231)',
        'road-width': 2,
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'highway_line',
    source: 'baremaps',
    'source-layer': 'highway',
    type: 'line',
    layout: {
        visibility: 'visible',
        'line-cap': 'round',
        'line-join': 'round',
    },
    filter: [
        'all',
        ['!=', ['get', 'bridge'], 'yes'],
        ['!=', ['get', 'tunnel'], 'yes'],
        ['!=', ['get', 'layer'], '-1'],
        ['!=', ['get', 'covered'], 'yes'],
    ],
});
