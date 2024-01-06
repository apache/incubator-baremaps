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
import {asLayerObject, withSortKeys} from "../../utils/utils.js";
import theme from "../../theme.js";

/**
 * These directives are based on the following source:
 * https://wiki.openstreetmap.org/wiki/OpenStreetMap_Carto/Symbols
 */
let directives = [
    // Amenity
    {
        'filter': [
            'all',
            ['==', ['get', 'amenity'], 'restaurant'],
            ['==', ['get', 'amenity'], 'food_court']
        ],
        'icon-image': 'restaurant',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'cafe'],
        'icon-image': 'cafe',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'fast_food'],
        'icon-image': 'fast_food',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'bar'],
        'icon-image': 'bar',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'pub'],
        'icon-image': 'pub',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'ice_cream'],
        'icon-image': 'ice_cream',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'biergarten'],
        'icon-image': 'biergarten',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'community_centre'],
        'icon-image': 'community_centre',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'library'],
        'icon-image': 'library',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'theatre'],
        'icon-image': 'theatre',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'cinema'],
        'icon-image': 'cinema',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'nightclub'],
        'icon-image': 'nightclub',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'arts_centre'],
        'icon-image': 'arts_centre',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'internet_cafe'],
        'icon-image': 'internet_cafe',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'casino'],
        'icon-image': 'casino',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'public_bookcase'],
        'icon-image': 'public_bookcase',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'public_bath'],
        'icon-image': 'public_bath',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'toilets'],
        'icon-image': 'toilets',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'recycling'],
        'icon-image': 'recycling',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'waste_basket'],
        'icon-image': 'waste_basket',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'waste_disposal'],
        'icon-image': 'waste_disposal',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'bench'],
        'icon-image': 'bench',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'shelter'],
        'icon-image': 'shelter',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'drinking_water'],
        'icon-image': 'drinking_water',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'fountain'],
        'icon-image': 'fountain',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'bbq'],
        'icon-image': 'bbq',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'shower'],
        'icon-image': 'shower',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'bank'],
        'icon-image': 'bank',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'atm'],
        'icon-image': 'atm',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'bureau_de_change'],
        'icon-image': 'bureau_de_change',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'pharmacy'],
        'icon-image': 'pharmacy',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'hospital'],
        'icon-image': 'hospital',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': [
            'any',
            ['==', ['get', 'amenity'], 'clinic'],
            ['==', ['get', 'amenity'], 'doctors']
        ],
        'icon-image': 'doctors',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'dentist'],
        'icon-image': 'dentist',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'veterinary'],
        'icon-image': 'veterinary',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'post_box'],
        'icon-image': 'post_box',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'post_office'],
        'icon-image': 'post_office',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    // {
    //     'filter': ['==', ['get', 'amenity'], 'parcel_locker'],
    //     'icon-image': 'parcel_locker',
    //     'icon-color': theme.pointIconAmenityIconColor,
    //     'text-color': theme.pointIconAmenityTextColor
    // },
    {
        'filter': ['==', ['get', 'amenity'], 'telephone'],
        'icon-image': 'telephone',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'parking'],
        'icon-image': 'parking',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': [
            'any',
            ['all',
                ['==', ['get', 'amenity'], 'parking'],
                ['==', ['get', 'parking'], 'lane'],
            ],
            ['all',
                ['==', ['get', 'amenity'], 'parking'],
                ['==', ['get', 'parking'], 'street_side']
            ]
        ],
        'icon-image': 'parking_subtle',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'fuel'],
        'icon-image': 'fuel',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'bicycle_parking'],
        'icon-image': 'bicycle_parking',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'bus_station'],
        'icon-image': 'amenity_bus_station',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'bicycle_rental'],
        'icon-image': 'rental_bicycle',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'taxi'],
        'icon-image': 'taxi',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'amenity'], 'vending_machine'],
            ['==', ['get', 'vending'], 'parking_tickets']
        ],
        'icon-image': 'parking_tickets',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'charging_station'],
        'icon-image': 'charging_station',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'car_rental'],
        'icon-image': 'rental_car',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'amenity'], 'parking_entrance'],
            ['==', ['get', 'parking'], 'underground']
        ],
        'icon-image': 'parking_entrance_underground',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'amenity'], 'vending_machine'],
            ['==', ['get', 'vending'], 'public_transport_tickets']
        ],
        'icon-image': 'public_transport_tickets',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'ferry_terminal'],
        'icon-image': 'ferry',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'motorcycle_parking'],
        'icon-image': 'motorcycle_parking',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'bicycle_repair_station'],
        'icon-image': 'bicycle_repair_station',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'boat_rental'],
        'icon-image': 'boat_rental',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'amenity'], 'parking_entrance'],
            ['==', ['get', 'parking'], 'multi-storey']
        ],
        'icon-image': 'parking_entrance_multi_storey',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'police'],
        'icon-image': 'police',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'townhall'],
        'icon-image': 'town_hall',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'fire_station'],
        'icon-image': 'firestation',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'social_facility'],
        'icon-image': 'social_facility',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'courthouse'],
        'icon-image': 'courthouse',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'prison'],
        'icon-image': 'prison',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'amenity'], 'place_of_worship'],
            ['==', ['get', 'religion'], 'christian']
        ],
        'icon-image': 'christian',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'amenity'], 'place_of_worship'],
            ['==', ['get', 'religion'], 'jewish']
        ],
        'icon-image': 'jewish',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'amenity'], 'place_of_worship'],
            ['==', ['get', 'religion'], 'muslim']
        ],
        'icon-image': 'muslim',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'amenity'], 'place_of_worship'],
            ['==', ['get', 'religion'], 'taoist']
        ],
        'icon-image': 'taoist',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'amenity'], 'place_of_worship'],
            ['==', ['get', 'religion'], 'hindu']
        ],
        'icon-image': 'hinduist',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'amenity'], 'place_of_worship'],
            ['==', ['get', 'religion'], 'buddhist']
        ],
        'icon-image': 'buddhist',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'amenity'], 'place_of_worship'],
            ['==', ['get', 'religion'], 'shinto']
        ],
        'icon-image': 'shintoist',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'amenity'], 'place_of_worship'],
            ['==', ['get', 'religion'], 'sikh']
        ],
        'icon-image': 'sikhist',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'amenity'], 'place_of_worship'],
            ['==', ['get', 'without or other religion'], '* value']
        ],
        'icon-image': 'place_of_worship',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'marketplace'],
        'icon-image': 'marketplace',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': [
            'any',
            ['==', ['get', 'amenity'], 'nursing_home'],
            ['==', ['get', 'amenity'], 'childcare']
        ],
        'icon-image': 'social_facility',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'amenity'], 'hunting_stand'],
        'icon-image': 'hunting_stand',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },

    // Historic
    // {
    //     filter: ['==', ['get', 'historic'], 'archaeological_site'],
    //     'icon-image': 'archaeological_site',
    //     'icon-color': theme.pointIconArchaeologicalSiteIconColor,
    //     'text-color': theme.pointIconArchaeologicalSiteTextColor,
    // },
    // {
    //     filter: ['==', ['get', 'historic'], 'castle'],
    //     'icon-image': 'castle',
    //     'icon-color': theme.pointIconCastleIconColor,
    //     'text-color': theme.pointIconCastleTextColor,
    // },
    // {
    //     filter: ['==', ['get', 'historic'], 'city_gate'],
    //     'icon-image': 'city_gate',
    //     'icon-color': theme.pointIconCityGateIconColor,
    //     'text-color': theme.pointIconCityGateTextColor,
    // },
    // {
    //     filter: ['==', ['get', 'historic'], 'fort'],
    //     'icon-image': 'fort',
    //     'icon-color': theme.pointIconFortIconColor,
    //     'text-color': theme.pointIconFortTextColor,
    // },
    // {
    //     filter: ['==', ['get', 'historic'], 'manor'],
    //     'icon-image': 'manor',
    //     'icon-color': theme.pointIconManorIconColor,
    //     'text-color': theme.pointIconManorTextColor,
    // },
    // {
    //     filter: ['==', ['get', 'historic'], 'memorial'],
    //     'icon-image': 'memorial',
    //     'icon-color': theme.pointIconMemorialIconColor,
    //     'text-color': theme.pointIconMemorialTextColor,
    // },
    // {
    //     filter: ['==', ['get', 'historic'], 'monument'],
    //     'icon-image': 'monument',
    //     'icon-color': theme.pointIconMonumentIconColor,
    //     'text-color': theme.pointIconMonumentTextColor,
    // },
    // {
    //     filter: ['==', ['get', 'historic'], 'wayside_cross'],
    //     'icon-image': 'wayside_cross',
    //     'icon-color': theme.pointIconWaysideCrossIconColor,
    //     'text-color': theme.pointIconWaysideCrossTextColor,
    // },
    // {
    //     filter: ['==', ['get', 'historic'], 'wayside_shrine'],
    //     'icon-image': 'wayside_shrine',
    //     'icon-color': theme.pointIconWayShrineIconColor,
    //     'text-color': theme.pointIconWaysideShrineTextColor,
    // },



    {
        'filter': ['==', ['get', 'historic'], 'memorial'],
        'icon-image': 'memorial',
        'icon-color': theme.pointIconHistoricIconColor,
        'text-color': theme.pointIconHistoricTextColor
    },
    {
        'filter': ['==', ['get', 'historic'], 'archaeological_site'],
        'icon-image': 'archaeological_site',
        'icon-color': theme.pointIconHistoricIconColor,
        'text-color': theme.pointIconHistoricTextColor
    },
    {
        'filter': ['==', ['get', 'historic'], 'wayside_shrine'],
        'icon-image': 'wayside_shrine',
        'icon-color': theme.pointIconHistoricIconColor,
        'text-color': theme.pointIconHistoricTextColor
    },
    {
        'filter': ['==', ['get', 'historic'], 'monument'],
        'icon-image': 'monument',
        'icon-color': theme.pointIconHistoricIconColor,
        'text-color': theme.pointIconHistoricTextColor
    },
    {
        'filter': ['==', ['get', 'historic'], 'castle'],
        'icon-image': 'castle',
        'icon-color': theme.pointIconHistoricIconColor,
        'text-color': theme.pointIconHistoricTextColor
    },
    {
        'filter': [
            'any',
            [
                'all',
                ['==', ['get', 'historic'], 'memorial'],
                ['==', ['get', 'memorial'], 'plaque']
            ],
            [
                'all',
                ['==', ['get', 'historic'], 'memorial'],
                ['==', ['get', 'memorial'], 'blue_plaque']
            ]
        ],
        'icon-image': 'plaque',
        'icon-color': theme.pointIconHistoricIconColor,
        'text-color': theme.pointIconHistoricTextColor
    },
    {
        'filter': [
            'any',
            [
                'all',
                ['==', ['get', 'historic'], 'memorial'],
                ['==', ['get', 'memorial'], 'statue']
            ],
            [
                'all',
                ['==', ['get', 'tourism'], 'artwork'],
                ['==', ['get', 'artwork_type'], 'statue']
            ]
        ],
        'icon-image': 'statue',
        'icon-color': theme.pointIconHistoricIconColor,
        'text-color': theme.pointIconHistoricTextColor
    },
    {
        'filter': [
            'any',
            ['==', ['get', 'historic'], 'memorial'],
            ['==', ['get', 'memorial'], 'stone']
        ],
        'icon-image': 'stone',
        'icon-color': theme.pointIconHistoricIconColor,
        'text-color': theme.pointIconHistoricTextColor
    },
    {
        'filter': [
            'any',
            [
                'all',
                ['==', ['get', 'historic'], 'castle'],
                ['==', ['get', 'castle_type'], 'palace']
            ],
            [
                'all',
                ['==', ['get', 'historic'], 'castle'],
                ['==', ['get', 'castle_type'], 'stately']
            ]
        ],
        'icon-image': 'palace',
        'icon-color': theme.pointIconHistoricIconColor,
        'text-color': theme.pointIconHistoricTextColor
    },
    {
        'filter': ['==', ['get', 'historic'], 'castle'],
        'icon-image': 'fortress',
        'icon-color': theme.pointIconHistoricIconColor,
        'text-color': theme.pointIconHistoricTextColor
    },
    {
        'filter': ['==', ['get', 'historic'], 'fort'],
        'icon-image': 'historic_fort',
        'icon-color': theme.pointIconHistoricIconColor,
        'text-color': theme.pointIconHistoricTextColor
    },
    {
        'filter': [
            'any',
            [
                'all',
                ['==', ['get', 'historic'], 'memorial'],
                ['==', ['get', 'memorial'], 'bust']
            ],
            [
                'all',
                ['==', ['get', 'tourism'], 'artwork'],
                ['==', ['get', 'artwork_type'], 'bust']
            ]
        ],
        'icon-image': 'bust',
        'icon-color': theme.pointIconHistoricIconColor,
        'text-color': theme.pointIconHistoricTextColor
    },
    {
        'filter': ['==', ['get', 'historic'], 'city_gate'],
        'icon-image': 'city_gate',
        'icon-color': theme.pointIconHistoricIconColor,
        'text-color': theme.pointIconHistoricTextColor
    },
    {
        'filter': [
            'any',
            ['==', ['get', 'historic'], 'manor'],
            [
                'all',
                ['==', ['get', 'historic'], 'castle'],
                ['==', ['get', 'castle_type'], 'manor']
            ]
        ],
        'icon-image': 'manor',
        'icon-color': theme.pointIconHistoricIconColor,
        'text-color': theme.pointIconHistoricTextColor
    },



    // Leisure
    {
        'filter': ['==', ['get', 'leisure'], 'playground'],
        'icon-image': 'playground',
        'icon-color': theme.pointIconLeisureColor,
        'text-color': theme.pointIconLeisureTextColor,
    },
    {
        'filter': [
            'any',
            ['==', ['get', 'leisure'], 'fitness_centre'],
            ['==', ['get', 'leisure'], 'fitness_station']
        ],
        'icon-image': 'fitness',
        'icon-color': theme.pointIconLeisureColor,
        'text-color': theme.pointIconLeisureTextColor,
    },
    {
        'filter': ['==', ['get', 'leisure'], 'golf_course'],
        'icon-image': 'golf',
        'icon-color': theme.pointIconLeisureColor,
        'text-color': theme.pointIconLeisureTextColor,
    },
    {
        'filter': [
            'any',
            ['==', ['get', 'leisure'], 'water_park'],
            ['==', ['get', 'leisure'], 'swimming_area'],
            ['==', ['get', 'leisure'], 'sports_centre'],
            ['==', ['get', 'sport'], 'swimming']
        ],
        'icon-image': 'water_park',
        'icon-color': theme.pointIconLeisureColor,
        'text-color': theme.pointIconLeisureTextColor,
    },
    {
        'filter': ['==', ['get', 'leisure'], 'sauna'],
        'icon-image': 'sauna',
        'icon-color': theme.pointIconLeisureColor,
        'text-color': theme.pointIconLeisureTextColor,
    },
    {
        'filter': ['==', ['get', 'leisure'], 'outdoor_seating'],
        'icon-image': 'outdoor_seating',
        'icon-color': theme.pointIconLeisureColor,
        'text-color': theme.pointIconLeisureTextColor,
    },
    {
        'filter': ['==', ['get', 'leisure'], 'amusement_arcade'],
        'icon-image': 'amusement_arcade',
        'icon-color': theme.pointIconLeisureColor,
        'text-color': theme.pointIconLeisureTextColor,
    },
    {
        'filter': ['==', ['get', 'leisure'], 'miniature_golf'],
        'icon-image': 'miniature_golf',
        'icon-color': theme.pointIconLeisureColor,
        'text-color': theme.pointIconLeisureTextColor,
    },
    {
        'filter': ['==', ['get', 'leisure'], 'beach_resort'],
        'icon-image': 'beach_resort',
        'icon-color': theme.pointIconLeisureColor,
        'text-color': theme.pointIconLeisureTextColor,
    },
    {
        'filter': ['==', ['get', 'leisure'], 'fishing'],
        'icon-image': 'fishing',
        'icon-color': theme.pointIconLeisureColor,
        'text-color': theme.pointIconLeisureTextColor,
    },
    {
        'filter': ['==', ['get', 'leisure'], 'bowling_alley'],
        'icon-image': 'bowling_alley',
        'icon-color': theme.pointIconLeisureColor,
        'text-color': theme.pointIconLeisureTextColor,
    },
    {
        'filter': ['==', ['get', 'leisure'], 'dog_park'],
        'icon-image': 'dog_park',
        'icon-color': theme.pointIconLeisureColor,
        'text-color': theme.pointIconLeisureTextColor,
    },
    {
        'filter': ['==', ['get', 'golf'], 'pin'],
        'icon-image': 'leisure_golf_pin',
        'icon-color': theme.pointIconLeisureColor,
        'text-color': theme.pointIconLeisureTextColor,
    },
    {
        'filter': ['==', ['get', 'leisure'], 'picnic_table'],
        'icon-image': 'picnic',
        'icon-color': theme.pointIconLeisureColor,
        'text-color': theme.pointIconLeisureTextColor,
    },
    {
        'filter': ['==', ['get', 'leisure'], 'firepit'],
        'icon-image': 'firepit',
        'icon-color': theme.pointIconLeisureColor,
        'text-color': theme.pointIconLeisureTextColor,
    },
    {
        'filter': ['==', ['get', 'leisure'], 'bird_hide'],
        'icon-image': 'bird_hide',
        'icon-color': theme.pointIconLeisureColor,
        'text-color': theme.pointIconLeisureTextColor,
    },

    {
        'filter': ['==', ['get', 'leisure'], 'slipway'],
        'icon-image': 'slipway',
        'icon-color': theme.pointIconLeisureColor,
        'text-color': theme.pointIconLeisureTextColor,
    },

    // Man-made
    {
        'filter': ['any', ['==', ['get', 'man_made'], 'storage_tank'], ['==', ['get', 'man_made'], 'silo']],
        'icon-image': 'storage_tank',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': ['==', ['get', 'man_made'], 'tower'],
        'icon-image': 'tower_generic',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': ['all', ['==', ['get', 'man_made'], 'tower'], ['==', ['get', 'tower:type'], 'communication']],
        'icon-image': 'tower_cantilever_communication',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': ['any', ['==', ['get', 'historic'], 'wayside_cross'], ['==', ['get', 'man_made'], 'cross']],
        'icon-image': 'christian',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': ['==', ['get', 'man_made'], 'water_tower'],
        'icon-image': 'water_tower',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': ['==', ['get', 'man_made'], 'obelisk'],
        'icon-image': 'obelisk',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': ['==', ['get', 'man_made'], 'mast'],
        'icon-image': 'mast',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': ['==', ['get', 'man_made'], 'chimney'],
        'icon-image': 'chimney',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': [
            'any',
            [
                'all',
                ['==', ['get', 'man_made'], 'tower'],
                ['==', ['get', 'tower:type'], 'observation']
            ],
            [
                'all',
                ['==', ['get', 'man_made'], 'tower'],
                ['==', ['get', 'tower:type'], 'watchtower']
            ]
        ],
        'icon-image': 'tower_observation',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'man_made'], 'tower'],
            ['==', ['get', 'tower:type'], 'bell_tower']
        ],
        'icon-image': 'tower_bell_tower',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'man_made'], 'tower'],
            ['==', ['get', 'tower:type'], 'lighting']
        ],
        'icon-image': 'tower_lighting',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': ['==', ['get', 'man_made'], 'lighthouse'],
        'icon-image': 'lighthouse',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },

    {
        'filter': ['==', ['get', 'man_made'], 'crane'],
        'icon-image': 'crane',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': ['==', ['get', 'man_made'], 'windmill'],
        'icon-image': 'windmill',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'man_made'], 'tower'],
            ['==', ['get', 'tower:type'], 'communication'],
            ['==', ['get', 'tower:construction'], 'lattice']
        ],
        'icon-image': 'tower_lattice_communication',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'man_made'], 'mast'],
            ['==', ['get', 'tower:type'], 'lighting']
        ],
        'icon-image': 'mast_lighting',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'man_made'], 'mast'],
            ['==', ['get', 'tower:type'], 'communication']
        ],
        'icon-image': 'mast_communications',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': ['==', ['get', 'man_made'], 'communications_tower'],
        'icon-image': 'communication_tower',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'man_made'], 'tower'],
            ['==', ['get', 'tower:type'], 'defensive']
        ],
        'icon-image': 'tower_defensive',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'man_made'], 'tower'],
            ['==', ['get', 'tower:type'], 'cooling']
        ],
        'icon-image': 'tower_cooling',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'man_made'], 'tower'],
            ['==', ['get', 'tower:construction'], 'lattice']
        ],
        'icon-image': 'tower_lattice',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'man_made'], 'tower'],
            ['==', ['get', 'tower:type'], 'lighting'],
            ['==', ['get', 'tower:construction'], 'lattice']
        ],
        'icon-image': 'tower_lattice_lighting',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'man_made'], 'tower'],
            ['==', ['get', 'tower:construction'], 'dish']
        ],
        'icon-image': 'tower_dish',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'man_made'], 'tower'],
            ['==', ['get', 'tower:construction'], 'dome']
        ],
        'icon-image': 'tower_dome',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'man_made'], 'telescope'],
            ['==', ['get', 'telescope:type'], 'radio']
        ],
        'icon-image': 'telescope_dish',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'man_made'], 'telescope'],
            ['==', ['get', 'telescope:type'], 'optical']
        ],
        'icon-image': 'telescope_dome',
        'icon-color': theme.pointIconManMadeIconColor,
        'text-color': theme.pointIconManMadeTextColor,
    },

    // Military
    {
        filter: ['==', ['get', 'military'], 'bunker'],
        'icon-image': 'bunker',
        'icon-color': theme.pointIconBunkerIconColor,
        'text-color': theme.pointIconBunkerTextColor,
    },

    // Natural
    {
        filter: ['==', ['get', 'natural'], 'spring'],
        'icon-image': 'spring',
        'icon-color': theme.pointIconSpringIconColor,
        'text-color': theme.pointIconSpringTextColor,
    },
    {
        filter: ['==', ['get', 'natural'], 'cave_entrance'],
        'icon-image': 'entrance',
        'icon-color': theme.pointIconCaveEntranceIconColor,
        'text-color': theme.pointIconCaveEntranceTextColor,
    },
    {
        filter: ['==', ['get', 'natural'], 'peak'],
        'icon-image': 'peak',
        'icon-color': theme.pointIconPeakIconColor,
        'text-color': theme.pointIconPeakTextColor,
    },
    {
        filter: ['==', ['get', 'natural'], 'saddle'],
        'icon-image': 'saddle',
        'icon-color': theme.pointIconSaddleIconColor,
        'text-color': theme.pointIconSaddleTextColor,
    },
    {
        filter: ['==', ['get', 'natural'], 'volcano'],
        'icon-image': 'volcano',
        'icon-color': theme.pointIconVolcanoIconColor,
        'text-color': theme.pointIconVolcanoTextColor,
    },

    // Railway: stations and stops
    {
        filter: ['==', ['get', 'railway'], 'halt'],
        'icon-image': 'place-6',
        'icon-color': theme.pointIconHaltIconColor,
        'text-color': theme.pointIconHaltTextColor,
    },
    {
        filter: ['==', ['get', 'railway'], 'station'],
        'icon-image': 'place-6',
        'icon-color': theme.pointIconStationIconColor,
        'text-color': theme.pointIconStationTextColor,
    },
    {
        filter: ['==', ['get', 'railway'], 'subway_entrance'],
        'icon-image': 'entrance',
        'icon-color': theme.pointIconSubwayEntranceIconColor,
        'text-color': theme.pointIconSubwayEntranceTextColor,
    },
    {
        filter: ['==', ['get', 'railway'], 'tram_stop'],
        'icon-image': 'tram_stop',
        'icon-color': theme.pointIconTramStopIconColor,
        'text-color': theme.pointIconTramStopTextColor,
    },

    // Railway: other railways
    {
        filter: ['==', ['get', 'railway'], 'crossing'],
        'icon-image': 'level_crossing',
        'icon-color': theme.pointIconCrossingIconColor,
        'text-color': theme.pointIconCrossingTextColor,
    },
    {
        filter: ['==', ['get', 'railway'], 'level_crossing'],
        'icon-image': 'level_crossing',
        'icon-color': theme.pointIconLevelCrossingIconColor,
        'text-color': theme.pointIconLevelCrossingTextColor,
    },

    // Tourism
    {
        filter: ['==', ['get', 'tourism'], 'alpine_hut'],
        'icon-image': 'alpine_hut',
        'icon-color': theme.pointIconAlpineHutIconColor,
        'text-color': theme.pointIconAlpineHutTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'apartment'],
        'icon-image': 'apartment',
        'icon-color': theme.pointIconApartmentIconColor,
        'text-color': theme.pointIconApartmentTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'artwork'],
        'icon-image': 'artwork',
        'icon-color': theme.pointIconArtworkIconColor,
        'text-color': theme.pointIconArtworkTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'camp_site'],
        'icon-image': 'camping',
        'icon-color': theme.pointIconCampSiteIconColor,
        'text-color': theme.pointIconCampSiteTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'caravan_site'],
        'icon-image': 'caravan_park',
        'icon-color': theme.pointIconCaravaneSiteIconColor,
        'text-color': theme.pointIconCaravaneSiteTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'chalet'],
        'icon-image': 'chalet',
        'icon-color': theme.pointIconChaletIconColor,
        'text-color': theme.pointIconChaletTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'gallery'],
        'icon-image': 'art',
        'icon-color': theme.pointIconGalleryIconColor,
        'text-color': theme.pointIconGalleryTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'guest_house'],
        'icon-image': 'guest_house',
        'icon-color': theme.pointIconGuestHouseIconColor,
        'text-color': theme.pointIconGuestHouseextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'hostel'],
        'icon-image': 'hostel',
        'icon-color': theme.pointIconHostelIconColor,
        'text-color': theme.pointIconHostelTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'motel'],
        'icon-image': 'motel',
        'icon-color': theme.pointIconMotelIconColor,
        'text-color': theme.pointIconMotelTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'museum'],
        'icon-image': 'museum',
        'icon-color': theme.pointIconMuseumIconColor,
        'text-color': theme.pointIconMuseumTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'picnic_site'],
        'icon-image': 'picnic',
        'icon-color': theme.pointIconPicnicSiteIconColor,
        'text-color': theme.pointIconPicnicSiteTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'viewpoint'],
        'icon-image': 'viewpoint',
        'icon-color': theme.pointIconViewpointIconColor,
        'text-color': theme.pointIconViewpointTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'wilderness_hut'],
        'icon-image': 'wilderness_hut',
        'icon-color': theme.pointIconWildernessHutIconColor,
        'text-color': theme.pointIconWildernessHutTextColor,
    },

    // Waterway: barriers on waterways
    {
        filter: ['==', ['get', 'waterway'], 'dam'],
        'icon-image': 'dam',
        'icon-color': theme.pointIconDamIconColor,
        'text-color': theme.pointIconDamTextColor,
    },
    {
        filter: ['==', ['get', 'waterway'], 'weir'],
        'icon-image': 'weir',
        'icon-color': theme.pointIconWeirIconColor,
        'text-color': theme.pointIconWeirTextColor,
    },
    {
        filter: ['==', ['get', 'waterway'], 'waterfall'],
        'icon-image': 'waterfall',
        'icon-color': theme.pointIconWaterfallIconColor,
        'text-color': theme.pointIconWaterfallTextColor,
    },
    {
        filter: ['==', ['get', 'waterway'], 'lock_gate'],
        'icon-image': 'lock_gate',
        'icon-color': theme.pointIconLockGateIconColor,
        'text-color': theme.pointIconLockGateTextColor,
    },

    // Shop
    {
        'filter': ['==', ['get', 'shop'], 'convenience'],
        'icon-image': 'convenience',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'supermarket'],
        'icon-image': 'supermarket',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': [
            'any',
            ['==', ['get', 'shop'], 'clothes'],
            ['==', ['get', 'shop'], 'fashion']
        ],
        'icon-image': 'clothes',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'hairdresser'],
        'icon-image': 'hairdresser',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'bakery'],
        'icon-image': 'bakery',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'car_repair'],
        'icon-image': 'car_repair',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['any',
            ['==', ['get', 'shop'], 'doityourself'],
            ['==', ['get', 'shop'], 'hardware']
        ],
        'icon-image': 'diy',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'car'],
        'icon-image': 'car',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['any',
            ['==', ['get', 'shop'], 'kiosk'],
            ['==', ['get', 'shop'], 'newsagent']
        ],
        'icon-image': 'newsagent',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'beauty'],
        'icon-image': 'beauty',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'amenity'], 'car_wash'],
        'icon-image': 'car_wash',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'butcher'],
        'icon-image': 'butcher',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['any',
            ['==', ['get', 'shop'], 'alcohol'],
            ['==', ['get', 'shop'], 'wine']
        ],
        'icon-image': 'alcohol',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'furniture'],
        'icon-image': 'furniture',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'florist'],
        'icon-image': 'florist',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'mobile_phone'],
        'icon-image': 'mobile_phone',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'electronics'],
        'icon-image': 'electronics',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'shoes'],
        'icon-image': 'shoes',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'car_parts'],
        'icon-image': 'car_parts',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': [
            'any',
            ['==', ['get', 'shop'], 'greengrocer'],
            ['==', ['get', 'shop'], 'farm']
        ],
        'icon-image': 'greengrocer',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': [
            'any',
            ['==', ['get', 'shop'], 'laundry'],
            ['==', ['get', 'shop'], 'dry_cleaning']
        ],
        'icon-image': 'laundry',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'optician'],
        'icon-image': 'optician',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': [
            'any',
            ['==', ['get', 'shop'], 'jewelry'],
            ['==', ['get', 'shop'], 'jewellery']
        ],
        'icon-image': 'jewelry',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'books'],
        'icon-image': 'library',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'gift'],
        'icon-image': 'gift',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'department_store'],
        'icon-image': 'department_store',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'bicycle'],
        'icon-image': 'bicycle',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': [
            'any',
            ['==', ['get', 'shop'], 'confectionery'],
            ['==', ['get', 'shop'], 'chocolate'],
            ['==', ['get', 'shop'], 'pastry']
        ],
        'icon-image': 'confectionery',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'variety_store'],
        'icon-image': 'variety_store',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'travel_agency'],
        'icon-image': 'travel_agency',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'sports'],
        'icon-image': 'sports',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'chemist'],
        'icon-image': 'chemist',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'computer'],
        'icon-image': 'computer',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'stationery'],
        'icon-image': 'stationery',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'pet'],
        'icon-image': 'pet',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'beverages'],
        'icon-image': 'beverages',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': [
            'any',
            ['==', ['get', 'shop'], 'cosmetics'],
            ['==', ['get', 'shop'], 'perfumery']
        ],
        'icon-image': 'perfumery',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'tyres'],
        'icon-image': 'tyres',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'motorcycle'],
        'icon-image': 'motorcycle',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'garden_centre'],
        'icon-image': 'garden_centre',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'copyshop'],
        'icon-image': 'copyshop',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'toys'],
        'icon-image': 'toys',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'deli'],
        'icon-image': 'deli',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'tobacco'],
        'icon-image': 'tobacco',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'seafood'],
        'icon-image': 'seafood',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'interior_decoration'],
        'icon-image': 'interior_decoration',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'ticket'],
        'icon-image': 'ticket',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': [
            'any',
            ['==', ['get', 'shop'], 'photo'],
            ['==', ['get', 'shop'], 'photo_studio'],
            ['==', ['get', 'shop'], 'photography']
        ],
        'icon-image': 'photo',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': [
            'any',
            ['==', ['get', 'shop'], 'trade'],
            ['==', ['get', 'shop'], 'wholesale']
        ],
        'icon-image': 'trade',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'outdoor'],
        'icon-image': 'outdoor',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'houseware'],
        'icon-image': 'houseware',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'art'],
        'icon-image': 'art',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'paint'],
        'icon-image': 'paint',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'fabric'],
        'icon-image': 'fabric',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'bookmaker'],
        'icon-image': 'bookmaker',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'second_hand'],
        'icon-image': 'second_hand',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'charity'],
        'icon-image': 'charity',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'bed'],
        'icon-image': 'bed',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'medical_supply'],
        'icon-image': 'medical_supply',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'hifi'],
        'icon-image': 'hifi',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'music'],
        'icon-image': 'music',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'coffee'],
        'icon-image': 'coffee',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'musical_instrument'],
        'icon-image': 'musical_instrument',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'tea'],
        'icon-image': 'tea',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'video'],
        'icon-image': 'video',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'bag'],
        'icon-image': 'bag',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'carpet'],
        'icon-image': 'carpet',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'video_games'],
        'icon-image': 'video_games',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'amenity'], 'vehicle_inspection'],
        'icon-image': 'vehicle_inspection',
        'icon-color': theme.pointIconShopIconColor,
        'text-color': theme.pointIconShopTextColor,
    },
    {
        'filter': ['==', ['get', 'shop'], 'dairy'],
        'icon-image': 'dairy',
        'icon-color': '',
        'text-color': ''
    },
    {
        'filter': ['==', ['get', 'shop'], 'massage'],
        'icon-image': 'massage',
        'icon-color': '',
        'text-color': ''
    },
    // {
    //     'filter': ['!=', ['get', 'shop'], 'yes'],
    //     'icon-image': 'place-4',
    //     'icon-color': theme.pointIconShopIconColor,
    //     'text-color': theme.pointIconShopTextColor,
    // },




    // To be classified
    {
        'filter': ['==', ['get', 'tourism'], 'artwork'],
        'icon-image': 'artwork',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': ['==', ['get', 'tourism'], 'museum'],
        'icon-image': 'museum',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
    {
        'filter': [
            'all',
            ['==', ['get', 'amenity'], 'vending_machine'],
            ['==', ['get', 'vending'], 'excrement_bags']
        ],
        'icon-image': 'excrement_bags',
        'icon-color': theme.pointIconAmenityIconColor,
        'text-color': theme.pointIconAmenityTextColor
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'icon',
    type: 'symbol',
    source: 'baremaps',
    'source-layer': 'point',
    'minzoom': 14,
    layout: {
        visibility: 'visible',
        'icon-size': 1,
        'icon-anchor': 'bottom',
        'text-font': ['Noto Sans Regular'],
        'text-size': 11,
        'text-field': ['get', 'name'],
        'text-anchor': 'top',
        'text-optional': true,
        'text-max-width': 5,
    },
    paint: {
        'icon-opacity': 1,
        'icon-translate-anchor': 'map',
        'icon-halo-color': theme.pointIconLayerIconHaloColor,
        'icon-halo-width': 1,
        'text-halo-width': 1,
        'text-halo-color': theme.pointIconLayerTextHaloColor,
    },
});
