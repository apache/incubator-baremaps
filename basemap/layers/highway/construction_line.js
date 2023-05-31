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

 let directives =[
     {
         filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['any',
                ['==', ['get', 'construction'], 'motorway'],
                ['==', ['get', 'construction'], 'motorway_link'],
            ]
         ],
         'line-color': 'rgb(233, 144, 161)',
         'road-width': 16,
     },
     {
         filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['any',
                ['==', ['get', 'construction'], 'trunk'],
                ['==', ['get', 'construction'], 'trunk_link'],
            ]
         ],
         'line-color': 'rgb(250, 193, 172)',
         'road-width': 12,
     },
     {
         filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['any',
                ['==', ['get', 'construction'], 'primary'],
                ['==', ['get', 'construction'], 'primary_link'],
            ]
         ],
         'line-color': 'rgb(253, 221, 179)',
         'road-width': 14,
     },
     {
         filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['any',
                ['==', ['get', 'construction'], 'secondary'],
                ['==', ['get', 'construction'], 'secondary_link'],
            ]
         ],
         'line-color': 'rgb(248, 250, 202)',
         'road-width': 12,
     },
     {
         filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['any',
                ['==', ['get', 'construction'], 'tertiary'],
                ['==', ['get', 'construction'], 'tertiary_link'],
            ]
         ],
         'line-color': 'rgb(190, 189, 188)',
         'road-width': 12,
     },
     {
         filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['==', ['get', 'construction'], 'unclassified'],
         ],
         'line-color': 'rgb(211, 207, 206)',
         'road-width': 8,
     },
     {
         filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['==', ['get', 'construction'], 'residential'],
         ],
         'line-color': 'rgb(211, 207, 206)',
         'road-width': 8,
     },
     {
         filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['==', ['get', 'construction'], 'living_street'],
         ],
         'line-color': 'rgb(207, 207, 207)',
         'road-width': 8,
     },
     {
         filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['==', ['get', 'construction'], 'service'],
         ],
         'line-color': 'rgb(213, 211, 211)',
         'road-width': 8,
     },
     {
        filter: [
           'all',
           ['==', ['get', 'highway'], 'construction'],
           ['==', ['get', 'construction'], 'raceway'],
        ],
        'line-color': 'rgb(213, 211, 211)',
        'road-width': 8,
    },
 ]
 
 export default asLayerObject(withSortKeys(directives), {
     id: 'highway_construction_line',
     source: 'baremaps',
     'source-layer': 'highway',
     type: 'line',
     layout: {
         visibility: 'visible',
         'line-cap': 'butt',
         'line-join': 'round',
     },
     paint: {
        'line-dasharray': [1, 1],
    },
 });
 