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
import colorScheme from "../../theme.js";

let directives = [
    {
        filter: ['==', ['get', 'highway'], 'bridleway'],
        'line-color': colorScheme.highwayDashBridlewayLineColor,
        'road-width': 1,
    },
    {
        filter: ['==', ['get', 'highway'], 'busway'],
        'line-color': colorScheme.highwayDashBuswayLineColor,
        'road-width': 1,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'cycleway'],
            [
                'all',
                ['==', ['get', 'highway'], 'path'],
                ['==', ['get', 'bicycle'], 'designated'],
            ],
        ],
        'line-color': colorScheme.highwayDashCyclewayLineColor,
        'road-width': 1,
    },
    {
        filter: [
            'any',
            [
                'all',
                ['==', ['get', 'highway'], 'footway'],
                ['==', ['get', 'access'], 'private'],
            ],
            [
                'all',
                ['==', ['get', 'highway'], 'service'],
                ['in', ['get', 'access'], ['literal', ['private', 'no']]],
            ],
        ],
        'line-color': colorScheme.highwayDashFootwayLineColor,
        'road-width': 1,
    },
    {
        filter: [
            'any',
            [
                'in',
                ['get', 'highway'],
                ['literal', ['sidewalk', 'crossing', 'steps']],
            ],
            [
                'all',
                ['==', ['get', 'highway'], 'footway'],
                ['!=', ['get', 'access'], 'private'],
            ],
            [
                'all',
                ['==', ['get', 'highway'], 'path'],
                ['!=', ['get', 'bicycle'], 'designated'],
            ],
        ],
        'line-color': colorScheme.highwayDashHighwayLineColor,
        'road-width': 1,
    },
    {
        filter: ['all', ['==', ['get', 'highway'], 'track']],
        'line-color': colorScheme.highwayDashTrackLineColor,
        'road-width': 1,
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'highway_dash',
    type: 'line',
    source: 'baremaps',
    'source-layer': 'highway',
    layout: {
        'line-cap': 'round',
        'line-join': 'round',
        visibility: 'visible',
    },
    paint: {
        'line-dasharray': [2, 2],
    },
});
