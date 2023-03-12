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
    id: 'background',
    type: 'background',
    source: 'baremaps',
    layout: {
        visibility: 'visible',
    },
    minzoom: 0,
    maxzoom: 24,
    paint: {
        'background-color': 'rgb(242, 239, 233)',
    },
}
