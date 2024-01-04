/**
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to you under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **/
import theme from "../../theme.js";
import {asLayerObject, withSortKeys} from "../../utils/utils.js";

let directives = [
    {
        "filter": ["!", ["has", "tunnel"]],
        "line-color": theme.waterwayLineColor,
        "line-width-stops": [4, 1, 14, 1],
    },
    {
        "filter": ["has", "tunnel"],
        "line-color": theme.waterwayTunnelColor,
        "line-width-stops": [4, 1, 14, 1],
    },
];

let layer = asLayerObject(withSortKeys(directives), {
    "id": "waterway",
    "type": "line",
    "source": "baremaps",
    "source-layer": "waterway",
    layout: {
        visibility: 'visible',
        'line-cap': 'round',
        'line-join': 'round',
    },
});

console.log(JSON.stringify(layer, null, 2));

export default layer;
