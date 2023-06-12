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
        filter: ['==', ['get', 'natural'], 'glacier'],
        'fill-color': colorScheme.naturalBackgroundGlacierFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'wood'],
        'fill-color': colorScheme.naturalBackgroundWoodFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'heath'],
        'fill-color': colorScheme.naturalBackgroundHeathFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'grassland'],
        'fill-color': colorScheme.naturalBackgroundGrasslandFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'bare_rock'],
        'fill-color': colorScheme.naturalBackgroundBareRockFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'scree'],
        'fill-color': colorScheme.naturalBackgroundScreeFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'shingle'],
        'fill-color': colorScheme.naturalBackgroundShingleFillColor
    },
    {
        filter: [
            'all',
            ['==', ['get', 'natural'], 'water'],
            ['==', ['get', 'water'], 'lake'],
        ],
        'fill-color': colorScheme.naturalBackgroundWaterFillColor,
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
