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


export let directives = [
    {
        'filter': [
            'all',
            ['==', ['get', 'railway'], 'rail'],
            ['!', ['has', 'service']],
        ],
        'line-color': colorScheme.railwayLineRailLineColor,
        'road-width': 10,
    },
    {
        'filter': ['all',
            ['==', ['get', 'railway'], 'rail'],
            ['has', 'service']
        ],
        'line-color': colorScheme.railwayLineAllRailsLineColor,
        'road-width': 6,
    },
    {
        'filter': ['==', ['get', 'railway'], 'subway'],
        'line-color': colorScheme.railwayLineSubwayLineColor,
        'road-width': 6,
    },
    {
        'filter': ['==', ['get', 'railway'], 'tram'],
        'line-color': colorScheme.railwayLineTramLineColor,
        'road-width': 6,
    },
    {
        'filter': ['==', ['get', 'railway'], 'preserved'],
        'line-color': colorScheme.railwayLinePreservedLineColor,
        'road-width': 6,
    },
    {
        'filter': ['==', ['get', 'railway'], 'funicular'],
        'line-color': colorScheme.railwayLineFunicularLineColor,
        'road-width': 6,
    },
    {
        'filter': ['==', ['get', 'railway'], 'monorail'],
        'line-color': colorScheme.railwayLineMonorailLineColor,
        'road-width': 6,
    },
    {
        'filter': ['==', ['get', 'railway'], 'light_rail'],
        'line-color': colorScheme.railwayLineLigthRailLineColor,
        'road-width': 6,
    },
    {
        'filter': ['==', ['get', 'railway'], 'construction'],
        'line-color': colorScheme.railwayLineConstructionLineColor,
        'road-width': 6,
    },
    {
        'filter': ['==', ['get', 'railway'], 'abandoned'],
        'line-color': colorScheme.railwayLineAbandonedLineColor,
        'road-width': 2,
    },
    {
        'filter': ['==', ['get', 'railway'], 'disused'],
        'line-color': colorScheme.railwayLineisuedLineColor,
        'road-width': 2,
    },
    {
        'filter': ['==', ['get', 'railway'], 'miniature'],
        'line-color': colorScheme.railwayLineMiniatureLineColor,
        'road-width': 2,
    },
    {
        'filter': ['==', ['get', 'railway'], 'narrow_gauge'],
        'line-color': colorScheme.railwayLineMarrowGaugeLineColor,
        'road-width': 2,
    },
];

export default asLayerObject(withSortKeys(directives), {
    'id': 'railway_line',
    'source': 'baremaps',
    'source-layer': 'railway',
    'type': 'line',
    'filter': ['!=', ['get', 'tunnel'], 'yes'],
    'layout': {
        'visibility': 'visible',
        'line-cap': 'round',
        'line-join': 'round',
    },
});
