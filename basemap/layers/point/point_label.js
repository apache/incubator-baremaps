/**
 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License
 is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 or implied. See the License for the specific language governing permissions and limitations under
 the License.
 **/
import theme from "../../theme.js";
import {asLayerObject, withSymbolSortKeys} from "../../utils/utils.js";

let directives = [
    {
        filter: [
            'all',
            ['==', ['get', 'capital'], 'yes'],
            ['==', ['get', 'place'], 'city']
        ],
        'label-color': theme.pointLabelCityLabelColor,
        'label-size': 16,
        'symbol-sort-key': ["-", ["to-number", ['get', 'population'], 0]],
    },
    {
        filter: [
            'all',
            ['!=', ['get', 'capital'], 'yes'],
            ['==', ['get', 'place'], 'city']
        ],
        'label-color': [theme.pointLabelCityFilterOneLabelColor, theme.pointLabelCityFilterTwoLabelColor],
        'label-size': 12,
        'symbol-sort-key': ["-", ["to-number", ['get', 'population'], 0]],
    },
    {
        filter: ['==', ['get', 'place'], 'town'],
        'label-size': 10,
        'label-color': [theme.pointLabelTownFilterOneLabelColor, theme.pointLabelTownFilterTwoLabelColor],

    },
    {
        filter: ['==', ['get', 'place'], 'village'],
        'label-size': 10,
        'label-color': theme.pointLabelVillageLabelColor,
    },
    {
        filter: ['==', ['get', 'place'], 'locality'],
        'label-size': 8,
        'label-color': theme.pointLabelLocalityLabelColor,
    },
    // {
    //     filter: [
    //         'in',
    //         ['get', 'place'],
    //         [
    //             'literal', [
    //                 'neighbourhood',
    //                 'quarter',
    //                 'hamlet',
    //                 'isolated_dwelling',
    //                 'islet'
    //             ]
    //         ]
    //     ],
    //     'text-size': 11,
    //     'text-color': theme.pointLabelPlaceTextColor,
    // },

];

export default asLayerObject(withSymbolSortKeys(directives), {
    id: 'point_label',
    type: 'symbol',
    source: 'baremaps',
    'source-layer': 'point',
    'minzoom': 2,
    'maxzoom': 24,
    layout: {
        visibility: 'visible',
        'text-font': ['Noto Sans Regular'],
        'text-field': ['get', 'name'],
    },
    paint: {
        'text-halo-color': theme.pointLabelPaintTextHaloColor,
        'text-halo-width': 1,
    },
});
