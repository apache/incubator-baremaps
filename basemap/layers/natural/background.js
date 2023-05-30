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
import colorScheme from "../../colorScheme.js";

let directives = [
    {
        filter: ['==', ['get', 'natural'], 'glacier'],
        'fill-color': colorScheme.directivesGlacierFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'wood'],
        'fill-color': colorScheme.directivesWoodFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'heath'],
        'fill-color': colorScheme.directivesHeathFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'grassland'],
        'fill-color': colorScheme.directivesGrasslandFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'bare_rock'],
        'fill-color': colorScheme.directivesBareRockFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'scree'],
        'fill-color': colorScheme.directivesScreeFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'shingle'],
        'fill-color': colorScheme.directivesShingleFillColor
    },
    {
        filter: [
            'all',
            ['==', ['get', 'natural'], 'water'],
            ['==', ['get', 'water'], 'lake'],
        ],
        'fill-color': colorScheme.directivesWaterFillColor,
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
