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
    id: 'power_plant',
    type: 'fill',
    filter: ['any', ['==', 'power', 'plant'], ['==', 'power', 'substation']],
    source: 'baremaps',
    'source-layer': 'power',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-color': 'rgb(226, 203, 222)',
        'fill-antialias': true,
        'fill-outline-color': 'rgb(171, 171, 171)',
    },
}
