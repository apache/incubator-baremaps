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
    id: 'man_made_pier_line',
    type: 'line',
    filter: ['==', ['get', 'man_made'], 'pier'],
    source: 'baremaps',
    'source-layer': 'man_made',
    layout: {
        visibility: 'visible',
    },
    minzoom: 12,
    paint: {
        'line-color': 'rgb(242, 239, 233)',
        'line-width': ['interpolate', ['exponential', 1], ['zoom'], 12, 0.5, 18, 2],
    },
}
