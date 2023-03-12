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
        'filter': ['==', ['get', 'aeroway'], 'runway'],
        'line-color': 'rgba(187, 187, 204, 1.0)',
        'road-width': 30,
    },
    {
        'filter': ['==', ['get', 'aeroway'], 'taxiway'],
        'line-color': 'rgba(187, 187, 204, 1.0)',
        'road-width': 14,
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'aeroway_line',
    type: 'line',
    source: 'baremaps',
    'source-layer': 'aeroway',
    filter: ['==', ['geometry-type'], 'LineString'],
    layout: {
        'line-cap': 'round',
        'line-join': 'round',
        visibility: 'visible',
    },
});
