/**
 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License
 is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 or implied. See the License for the specific language governing permissions and limitations under
 the License.
 **/
import {withFillSortKey} from "../../utils/utils.js";
import theme from "../../theme.js";

let directives = [
    {
        filter: ['==', 'leisure', 'nature_reserve'],
        'line-width': 5,
        'line-color': theme.leisureNatureReserveLineColor,
    },
];

export default {
    id: 'leisure_nature_reserve',
    type: 'line',
    source: 'baremaps',
    'source-layer': 'leisure',
    layout: {
        visibility: 'visible',
    },
    directives: directives.map(withFillSortKey),
}
