import layer from '../../utils/layer.js'

export default {
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
        'icon-halo-color': 'rgba(255, 255, 255, 0.8)',
        'icon-halo-width': 1,
        'text-halo-width': 1,
        'text-halo-color': 'rgba(255, 255, 255, 0.8)',
    },
    directives: [
        // Amenity: sustenance
        {
            filter: ['==', ['get', 'amenity'], 'bar'],
            'icon-image': 'bar',
            'icon-color': 'rgb(199, 116, 0)',
            'text-color': 'rgb(199, 116, 0)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'biergarten'],
            'icon-image': 'biergarten',
            'icon-color': 'rgb(199, 116, 0)',
            'text-color': 'rgb(199, 116, 0)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'cafe'],
            'icon-image': 'cafe',
            'icon-color': 'rgb(199, 116, 0)',
            'text-color': 'rgb(199, 116, 0)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'fast_food'],
            'icon-image': 'fast_food',
            'icon-color': 'rgb(199, 116, 0)',
            'text-color': 'rgb(199, 116, 0)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'food_court'],
            'icon-image': 'food_court',
            'icon-color': 'rgb(199, 116, 0)',
            'text-color': 'rgb(199, 116, 0)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'ice_cream'],
            'icon-image': 'ice_cream',
            'icon-color': 'rgb(199, 116, 0)',
            'text-color': 'rgb(199, 116, 0)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'pub'],
            'icon-image': 'pub',
            'icon-color': 'rgb(199, 116, 0)',
            'text-color': 'rgb(199, 116, 0)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'restaurant'],
            'icon-image': 'restaurant',
            'icon-color': 'rgb(199, 116, 0)',
            'text-color': 'rgb(199, 116, 0)',
        },

        // Amenity: education
        // {
        //     filter: ['==', ['get', 'amenity'], 'driving_school'],
        //     'icon-image': 'driving_school',
        //     'icon-color': 'rgb(172, 58, 173)',
        //     'text-color': 'rgb(172, 58, 173)',
        // },
        {
            filter: ['==', ['get', 'amenity'], 'library'],
            'icon-image': 'library',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },

        // Amenity: transportation
        {
            filter: ['==', ['get', 'amenity'], 'bicycle_parking'],
            'icon-image': 'bicycle_parking',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'bicycle_repair_station'],
            'icon-image': 'bicycle_repair_station',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'bicycle_rental'],
            'icon-image': 'rental_bicycle',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'boat_rental'],
            'icon-image': 'boat_rental',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'bus_station'],
            'icon-image': 'bus_station',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'car_rental'],
            'icon-image': 'rental_car',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'car_wash'],
            'icon-image': 'car_wash',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'vehicle_inspection'],
            'icon-image': 'vehicle_inspection',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'charging_station'],
            'icon-image': 'charging_station',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'ferry_terminal'],
            'icon-image': 'ferry',
            'icon-color': 'rgb(132, 97, 196)',
            'text-color': 'rgb(132, 97, 196)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'fuel'],
            'icon-image': 'fuel',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'motorcycle_parking'],
            'icon-image': 'motorcycle_parking',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'parking'],
            'icon-image': 'parking',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'parking_entrance'],
            'icon-image': 'entrance',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'taxi'],
            'icon-image': 'taxi',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },

        // Amenity: financial
        {
            filter: ['==', ['get', 'amenity'], 'atm'],
            'icon-image': 'atm',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'bank'],
            'icon-image': 'bank',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'bureau_de_change'],
            'icon-image': 'bureau_de_change',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },

        // Amenity: healthcare
        {
            filter: ['==', ['get', 'amenity'], 'clinic'],
            'icon-image': 'hospital',
            'icon-color': 'rgb(191, 0, 0)',
            'text-color': 'rgb(191, 0, 0)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'dentist'],
            'icon-image': 'dentist',
            'icon-color': 'rgb(191, 0, 0)',
            'text-color': 'rgb(191, 0, 0)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'doctors'],
            'icon-image': 'doctors',
            'icon-color': 'rgb(191, 0, 0)',
            'text-color': 'rgb(191, 0, 0)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'hospital'],
            'icon-image': 'hospital',
            'icon-color': 'rgb(191, 0, 0)',
            'text-color': 'rgb(191, 0, 0)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'nursing_home'],
            'icon-image': 'nursing_home',
            'icon-color': 'rgb(76, 76, 0)',
            'text-color': 'rgb(76, 76, 0)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'pharmacy'],
            'icon-image': 'pharmacy',
            'icon-color': 'rgb(191, 0, 0)',
            'text-color': 'rgb(191, 0, 0)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'social_facility'],
            'icon-image': 'social_facility',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'veterinary'],
            'icon-image': 'veterinary',
            'icon-color': 'rgb(191, 0, 0)',
            'text-color': 'rgb(191, 0, 0)',
        },

        // Amenity: entertainment, arts & culture
        {
            filter: ['==', ['get', 'amenity'], 'arts_centre'],
            'icon-image': 'arts_centre',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'casino'],
            'icon-image': 'casino',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'cinema'],
            'icon-image': 'cinema',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'community_centre'],
            'icon-image': 'community_centre',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'fountain'],
            'icon-image': 'fountain',
            'icon-color': 'rgb(87, 104, 236)',
            'text-color': 'rgb(87, 104, 236)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'nightclub'],
            'icon-image': 'nightclub',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'public_bookcase'],
            'icon-image': 'public_bookcase',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'theatre'],
            'icon-image': 'theatre',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },

        // Amenity: public service
        {
            filter: ['==', ['get', 'amenity'], 'courthouse'],
            'icon-image': 'courthouse',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'fire_station'],
            'icon-image': 'firestation',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'police'],
            'icon-image': 'police',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'post_box'],
            'icon-image': 'post_box',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'post_office'],
            'icon-image': 'post_office',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'prison'],
            'icon-image': 'prison',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'townhall'],
            'icon-image': 'town_hall',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },

        // Amenity: facilities
        {
            filter: ['==', ['get', 'amenity'], 'bbq'],
            'icon-image': 'bbq',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'bench'],
            'icon-image': 'bench',
            'icon-color': 'rgb(102, 102, 102)',
            'text-color': 'rgb(102, 102, 102)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'drinking_water'],
            'icon-image': 'drinking_water',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'shelter'],
            'icon-image': 'shelter',
            'icon-color': 'rgb(102, 102, 102)',
            'text-color': 'rgb(102, 102, 102)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'shower'],
            'icon-image': 'shower',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'telephone'],
            'icon-image': 'telephone',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'toilets'],
            'icon-image': 'toilets',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },

        // Amenity: waste management
        {
            filter: ['==', ['get', 'amenity'], 'recycling'],
            'icon-image': 'recycling',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'waste_basket'],
            'icon-image': 'waste_basket',
            'icon-color': 'rgb(102, 102, 102)',
            'text-color': 'rgb(102, 102, 102)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'waste_disposal'],
            'icon-image': 'waste_disposal',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },

        // Amenity: Others
        {
            filter: ['==', ['get', 'amenity'], 'childcare'],
            'icon-image': 'place-6',
            'icon-color': 'rgb(76, 76, 0)',
            'text-color': 'rgb(76, 76, 0)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'hunting_stand'],
            'icon-image': 'hunting_stand',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'internet_cafe'],
            'icon-image': 'internet_cafe',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'marketplace'],
            'icon-image': 'marketplace',
            'icon-color': 'rgb(172, 58, 173)',
            'text-color': 'rgb(172, 58, 173)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'place_of_worship'],
            'icon-image': 'place_of_worship',
            'icon-color': 'rgb(0, 0, 0)',
            'text-color': 'rgb(0, 0, 0)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'public_bath'],
            'icon-image': 'public_bath',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },

        // Historic
        {
            filter: ['==', ['get', 'historic'], 'archaeological_site'],
            'icon-image': 'archaeological_site',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'historic'], 'castle'],
            'icon-image': 'castle',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'historic'], 'city_gate'],
            'icon-image': 'city_gate',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },
        {
            filter: ['==', ['get', 'historic'], 'fort'],
            'icon-image': 'fort',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'historic'], 'manor'],
            'icon-image': 'manor',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'historic'], 'memorial'],
            'icon-image': 'memorial',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'historic'], 'monument'],
            'icon-image': 'monument',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'historic'], 'wayside_cross'],
            'icon-image': 'wayside_cross',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },
        {
            filter: ['==', ['get', 'historic'], 'wayside_shrine'],
            'icon-image': 'wayside_shrine',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },

        // Leisure
        {
            filter: ['==', ['get', 'leisure'], 'amusement_arcade'],
            'icon-image': 'amusement_arcade',
            'icon-color': 'rgb(13, 134, 22)',
            'text-color': 'rgb(13, 134, 22)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'beach_resort'],
            'icon-image': 'beach_resort',
            'icon-color': 'rgb(13, 134, 22)',
            'text-color': 'rgb(13, 134, 22)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'bird_hide'],
            'icon-image': 'bird_hide',
            'icon-color': 'rgb(13, 134, 22)',
            'text-color': 'rgb(13, 134, 22)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'bowling_alley'],
            'icon-image': 'bowling_alley',
            'icon-color': 'rgb(13, 134, 22)',
            'text-color': 'rgb(13, 134, 22)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'firepit'],
            'icon-image': 'firepit',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'fishing'],
            'icon-image': 'fishing',
            'icon-color': 'rgb(13, 134, 22)',
            'text-color': 'rgb(13, 134, 22)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'fitness_centre'],
            'icon-image': 'sports',
            'icon-color': 'rgb(13, 134, 22)',
            'text-color': 'rgb(13, 134, 22)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'fitness_station'],
            'icon-image': 'sports',
            'icon-color': 'rgb(13, 134, 22)',
            'text-color': 'rgb(13, 134, 22)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'golf_course'],
            'icon-image': 'golf_course',
            'icon-color': 'rgb(13, 134, 22)',
            'text-color': 'rgb(13, 134, 22)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'miniature_golf'],
            'icon-image': 'miniature_golf',
            'icon-color': 'rgb(13, 134, 22)',
            'text-color': 'rgb(13, 134, 22)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'outdoor_seating'],
            'icon-image': 'outdoor_seating',
            'icon-color': 'rgb(13, 134, 22)',
            'text-color': 'rgb(13, 134, 22)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'picnic_table'],
            'icon-image': 'picnic',
            'icon-color': 'rgb(102, 102, 102)',
            'text-color': 'rgb(102, 102, 102)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'playground'],
            'icon-image': 'playground',
            'icon-color': 'rgb(13, 134, 22)',
            'text-color': 'rgb(13, 134, 22)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'sauna'],
            'icon-image': 'sauna',
            'icon-color': 'rgb(13, 134, 22)',
            'text-color': 'rgb(13, 134, 22)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'slipway'],
            'icon-image': 'slipway',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'swimming_area'],
            'icon-image': 'swimming_area',
            'icon-color': 'rgb(13, 134, 22)',
            'text-color': 'rgb(13, 134, 22)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'water_park'],
            'icon-image': 'water_park',
            'icon-color': 'rgb(13, 134, 22)',
            'text-color': 'rgb(13, 134, 22)',
        },

        // Man-made
        {
            filter: ['==', ['get', 'man_made'], 'chimney'],
            'icon-image': 'chimney',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },
        {
            filter: ['==', ['get', 'man_made'], 'communications_tower'],
            'icon-image': 'communications_tower',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },
        {
            filter: ['==', ['get', 'man_made'], 'crane'],
            'icon-image': 'crane',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },
        {
            filter: ['==', ['get', 'man_made'], 'cross'],
            'icon-image': 'cross',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },
        {
            filter: ['==', ['get', 'man_made'], 'lighthouse'],
            'icon-image': 'lighthouse',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },
        {
            filter: ['==', ['get', 'man_made'], 'mast'],
            'icon-image': 'mast',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },
        {
            filter: ['==', ['get', 'man_made'], 'obelisk'],
            'icon-image': 'obelisk',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'man_made'], 'silo'],
            'icon-image': 'silo',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },
        {
            filter: ['==', ['get', 'man_made'], 'storage_tank'],
            'icon-image': 'storage_tank',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },
        {
            filter: ['==', ['get', 'man_made'], 'telescope'],
            'icon-image': 'telescope',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },
        {
            filter: ['==', ['get', 'man_made'], 'tower'],
            'icon-image': 'tower_generic',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },
        {
            filter: ['==', ['get', 'man_made'], 'water_tower'],
            'icon-image': 'water_tower',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },
        {
            filter: ['==', ['get', 'man_made'], 'windmill'],
            'icon-image': 'windmill',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },

        // Military
        {
            filter: ['==', ['get', 'military'], 'bunker'],
            'icon-image': 'bunker',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },

        // Natural
        {
            filter: ['==', ['get', 'natural'], 'spring'],
            'icon-image': 'spring',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'natural'], 'cave_entrance'],
            'icon-image': 'entrance',
            'icon-color': 'rgb(85, 85, 85)',
            'text-color': 'rgb(85, 85, 85)',
        },
        {
            filter: ['==', ['get', 'natural'], 'peak'],
            'icon-image': 'peak',
            'icon-color': 'rgb(209, 144, 85)',
            'text-color': 'rgb(209, 144, 85)',
        },
        {
            filter: ['==', ['get', 'natural'], 'saddle'],
            'icon-image': 'saddle',
            'icon-color': 'rgb(209, 144, 85)',
            'text-color': 'rgb(209, 144, 85)',
        },
        {
            filter: ['==', ['get', 'natural'], 'volcano'],
            'icon-image': 'volcano',
            'icon-color': 'rgb(212, 0, 0)',
            'text-color': 'rgb(212, 0, 0)',
        },

        // Railway: stations and stops
        {
            filter: ['==', ['get', 'railway'], 'halt'],
            'icon-image': 'place-6',
            'icon-color': 'rgb(122, 129, 177)',
            'text-color': 'rgb(122, 129, 177)',
        },
        {
            filter: ['==', ['get', 'railway'], 'station'],
            'icon-image': 'place-6',
            'icon-color': 'rgb(122, 129, 177)',
            'text-color': 'rgb(122, 129, 177)',
        },
        {
            filter: ['==', ['get', 'railway'], 'subway_entrance'],
            'icon-image': 'entrance',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'railway'], 'tram_stop'],
            'icon-image': 'tram_stop',
            'icon-color': 'rgb(122, 129, 177)',
            'text-color': 'rgb(122, 129, 177)',
        },

        // Railway: other railways
        {
            filter: ['==', ['get', 'railway'], 'crossing'],
            'icon-image': 'level_crossing',
            'icon-color': 'rgb(102, 102, 102)',
            'text-color': 'rgb(102, 102, 102)',
        },
        {
            filter: ['==', ['get', 'railway'], 'level_crossing'],
            'icon-image': 'level_crossing',
            'icon-color': 'rgb(102, 102, 102)',
            'text-color': 'rgb(102, 102, 102)',
        },

        // Tourism
        {
            filter: ['==', ['get', 'tourism'], 'alpine_hut'],
            'icon-image': 'alpine_hut',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'tourism'], 'apartment'],
            'icon-image': 'apartment',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'tourism'], 'artwork'],
            'icon-image': 'artwork',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'tourism'], 'artwork'],
            'icon-image': 'artwork',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'tourism'], 'camp_site'],
            'icon-image': 'camping',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'tourism'], 'caravan_site'],
            'icon-image': 'caravan_park',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'tourism'], 'chalet'],
            'icon-image': 'chalet',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'tourism'], 'gallery'],
            'icon-image': 'art',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'tourism'], 'guest_house'],
            'icon-image': 'guest_house',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'tourism'], 'hostel'],
            'icon-image': 'hostel',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'tourism'], 'hotel'],
            'icon-image': 'hotel',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'tourism'], 'motel'],
            'icon-image': 'motel',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'tourism'], 'museum'],
            'icon-image': 'museum',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'tourism'], 'picnic_site'],
            'icon-image': 'picnic',
            'icon-color': 'rgb(102, 102, 102)',
            'text-color': 'rgb(102, 102, 102)',
        },
        {
            filter: ['==', ['get', 'tourism'], 'viewpoint'],
            'icon-image': 'viewpoint',
            'icon-color': 'rgb(115, 74, 7)',
            'text-color': 'rgb(115, 74, 7)',
        },
        {
            filter: ['==', ['get', 'tourism'], 'wilderness_hut'],
            'icon-image': 'wilderness_hut',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },

        // Waterway: barriers on waterways
        {
            filter: ['==', ['get', 'waterway'], 'dam'],
            'icon-image': 'dam',
            'icon-color': 'rgb(173, 173, 173)',
            'text-color': 'rgb(173, 173, 173)',
        },
        {
            filter: ['==', ['get', 'waterway'], 'weir'],
            'icon-image': 'weir',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'waterway'], 'waterfall'],
            'icon-image': 'waterfall',
            'icon-color': 'rgb(0, 146, 219)',
            'text-color': 'rgb(0, 146, 219)',
        },
        {
            filter: ['==', ['get', 'waterway'], 'lock_gate'],
            'icon-image': 'lock_gate',
            'icon-color': 'rgb(173, 173, 173)',
            'text-color': 'rgb(173, 173, 173)',
        },
    ],
}
