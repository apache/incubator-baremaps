/**
 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License
 is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 or implied. See the License for the specific language governing permissions and limitations under
 the License.
 **/
import {withSortKeys, asLayerObject} from "../../../basemap/utils/utils.js";

let directives = [
    {
        filter: ['==', ['get', 'landcover'], 'snow'],
        'fill-color': '#ffffff',
    },
    {
        filter: ['==', ['get', 'landcover'], 'crop'],
        'fill-color': '#f0e68c',
    },
    {
        filter: ['==', ['get', 'landcover'], 'urban'],
        'fill-color': '#808080',
    },
    {
        filter: ['==', ['get', 'landcover'], 'trees'],
        'fill-color': '#228b22',
    },
    {
        filter: ['==', ['get', 'landcover'], 'grass'],
        'fill-color': '#7cfc00',
    },
    {
        filter: ['==', ['get', 'landcover'], 'barren'],
        'fill-color': '#d2b48c',
    },
    {
        filter: ['==', ['get', 'landcover'], 'shrub'],
        'fill-color': '#8b4513',
    }
];

export default asLayerObject(withSortKeys(directives), {
    id: 'landcover',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'landcover',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': false,
    },
});