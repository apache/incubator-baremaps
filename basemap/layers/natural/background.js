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
        filter: ['==', ['get', 'natural'], 'glacier'],
        'fill-color': 'rgb(221, 236, 236)'
    },
    {
        filter: ['==', ['get', 'natural'], 'wood'],
        'fill-color': 'rgb(157, 202, 138)'
    },
    {
        filter: ['==', ['get', 'natural'], 'heath'],
        'fill-color': 'rgb(214, 217, 159)'
    },
    {
        filter: ['==', ['get', 'natural'], 'grassland'],
        'fill-color': 'rgb(207, 236, 177)'
    },
    {
        filter: ['==', ['get', 'natural'], 'bare_rock'],
        'fill-color': 'rgb(217, 212, 206)'
    },
    {
        filter: ['==', ['get', 'natural'], 'scree'],
        'fill-color': 'rgb(232, 223, 216)'
    },
    {
        filter: ['==', ['get', 'natural'], 'shingle'],
        'fill-color': 'rgb(232, 223, 216)'
    },
    {
        filter: [
            'all',
            ['==', ['get', 'natural'], 'water'],
            ['==', ['get', 'water'], 'lake'],
        ],
        'fill-color': 'rgb(170, 211, 223)',
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'natural',
    type: 'fill',
    //filter: ['==', ['get', 'type'], 'Polygon'],
    source: 'baremaps',
    'source-layer': 'natural',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': true,
    },
});
