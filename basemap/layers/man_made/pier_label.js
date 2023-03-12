/**
 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License
 is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 or implied. See the License for the specific language governing permissions and limitations under
 the License.
 **/
export default {
    id: 'man_made_pier_label',
    type: 'symbol',
    filter: ['all', ['==', 'man_made', 'pier']],
    source: 'baremaps',
    'source-layer': 'man_made',
    layout: {
        'text-field': ['get', 'name'],
        'text-font': ['Noto Sans Regular'],
        'symbol-placement': 'line-center',
        'text-size': [
            'interpolate',
            ['exponential', 1],
            ['zoom'],
            15,
            8,
            16,
            11,
            20,
            11,
        ],
        visibility: 'visible',
    },
    minzoom: 15,
    paint: {
        'text-halo-color': 'rgba(255,255,255,0.8)',
        'text-halo-width': 1.2,
    },
}
