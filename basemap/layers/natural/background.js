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
        filter: ['==', ['get', 'natural'], 'glacier'],
        'fill-color': theme.naturalBackgroundGlacierFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'wood'],
        'fill-color': theme.naturalBackgroundWoodFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'heath'],
        'fill-color': theme.naturalBackgroundHeathFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'grassland'],
        'fill-color': theme.naturalBackgroundGrasslandFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'bare_rock'],
        'fill-color': theme.naturalBackgroundBareRockFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'scree'],
        'fill-color': theme.naturalBackgroundScreeFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'shingle'],
        'fill-color': theme.naturalBackgroundShingleFillColor
    },
    {
        filter: [
            'all',
            ['==', ['get', 'natural'], 'water'],
            ['==', ['get', 'water'], 'lake'],
        ],
        'fill-color': theme.naturalBackgroundWaterFillColor,
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
