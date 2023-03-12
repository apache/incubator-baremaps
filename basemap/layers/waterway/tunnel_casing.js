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
    "id": "waterway_tunnel_casing",
    "type": "line",
    "filter": [
        "any",
        ["==", "tunnel", "yes"],
        ["==", "tunnel", "culvert"]
    ],
    "source": "baremaps",
    "source-layer": "waterway",
    "layout": {
        "visibility": "visible"
    },
    "paint": {
        "line-width": [
            "interpolate", ["exponential", 1.2], ["zoom"], 4, 0, 20,
            12
        ],
        "line-color": "rgb(170, 211, 223)",
        "line-dasharray": [0.3, 0.15]
    }
}