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
        filter: ['==', ['get', 'natural'], 'beach'],
        'fill-color': 'rgb(255, 241, 186)'
    },
    {
        filter: ['==', ['get', 'natural'], 'sand'],
        'fill-color': 'rgb(240, 229, 196)'
    },
    {
        filter: ['==', ['get', 'natural'], 'scrub'],
        'fill-color': 'rgb(201, 216, 173)'
    },
    {
        filter: [
            'all',
            ['==', ['get', 'natural'], 'water'],
            ['!=', ['get', 'water'], 'lake'],
        ],
        'fill-color': 'rgb(170, 211, 223)',
    },
    {
        filter: ['==', ['get', 'natural'], 'wetland'],
        'fill-color': 'rgb(213, 231, 211)'
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'natural_overlay',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'natural',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': true,
    },
});
