/**
 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License
 is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 or implied. See the License for the specific language governing permissions and limitations under
 the License.
 **/
import {asLayerObject} from "../../utils/utils.js";

let directives = [
    {
        filter: ['==', ['get', 'amenity'], 'fountain'],
        'fill-color': 'rgb(170, 211, 223)',
        'fill-outline-color': 'rgb(170, 211, 223)',
    },
];

export default asLayerObject(directives, {
    id: 'amenity_fill_2',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'amenity',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': true,
    },
});
