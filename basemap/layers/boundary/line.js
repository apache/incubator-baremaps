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
        filter: ['==', ['get', 'admin_level'], "0"],
        'line-color': 'rgb(207, 155, 203)',
    },
    {
        filter: ['==', ['get', 'admin_level'], "1"],
        'line-color': 'rgb(207, 155, 203)',
    },
    {
        filter: ['==', ['get', 'admin_level'], "2"],
        'line-color': 'rgb(207, 155, 203)',
    },
    {
        filter: ['==', ['get', 'admin_level'], "3"],
        'line-color': 'rgb(207, 155, 203)',
    },
    {
        filter: ['==', ['get', 'admin_level'], "4"],
        'line-color': 'rgb(207, 155, 203)',
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'boundary',
    type: 'line',
    source: 'baremaps',
    'source-layer': 'boundary',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'line-dasharray': [4, 1, 1, 1],
    },
});
