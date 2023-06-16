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
        filter: ['==', ['get', 'natural'], 'wetland'],
        'fill-color': theme.naturalOverlayWetlandFillColor
    },
    {
        filter: [
            'all',
            ['==', ['get', 'natural'], 'beach'],
            ['==', ['get', 'surface'], 'gravel']
        ],
        'fill-color': theme.naturalOverlayBeachGravelFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'beach'],
        'fill-color': theme.naturalOverlayBeachFillColor
    },
    {
        filter: ['==', ['get', 'natural'], 'sand'],
        'fill-color': theme.naturalOverlaySandFillColor
    },
    {
        filter: [
            'all',
            ['==', ['get', 'natural'], 'water'],
            ['!=', ['get', 'water'], 'lake'],
        ],
        'fill-color': theme.naturalOverlayLakeFillColor
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
