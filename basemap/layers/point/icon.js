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
import theme from "../../theme.js";

let directives = [
    // Amenity: sustenance
    {
        filter: ['==', ['get', 'amenity'], 'bar'],
        'icon-image': 'bar',
        'icon-color': theme.directivesBarIconColor,
        'text-color': theme.directivesBarTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'biergarten'],
        'icon-image': 'biergarten',
        'icon-color': theme.directivesBiergartenIconColor,
        'text-color': theme.directivesBiergartenTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'cafe'],
        'icon-image': 'cafe',
        'icon-color': theme.directivesCafeIconColor,
        'text-color': theme.directivesCafeTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'fast_food'],
        'icon-image': 'fast_food',
        'icon-color': theme.directivesFastFoodIconColor,
        'text-color': theme.directivesFastFoodTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'food_court'],
        'icon-image': 'food_court',
        'icon-color': theme.directivesFoodCourtIconColor,
        'text-color': theme.directivesFoodCourtTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'ice_cream'],
        'icon-image': 'ice_cream',
        'icon-color': theme.directivesIceCreamIconColor,
        'text-color': theme.directivesIceCreamTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'pub'],
        'icon-image': 'pub',
        'icon-color': theme.directivesPubIconColor,
        'text-color': theme.directivesPubTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'restaurant'],
        'icon-image': 'restaurant',
        'icon-color': theme.directivesRestaurantIconColor,
        'text-color': theme.directivesRestaurantTextColor,
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
        'icon-color': theme.directivesLibraryIconColor,
        'text-color': theme.directivesLibraryTextColor,
    },

    // Amenity: transportation
    {
        filter: ['==', ['get', 'amenity'], 'bicycle_parking'],
        'icon-image': 'bicycle_parking',
        'icon-color': theme.directivesBicycleParkingIconColor,
        'text-color': theme.directivesBicycleParkingTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bicycle_repair_station'],
        'icon-image': 'bicycle_repair_station',
        'icon-color': theme.directivesBicycleRepairStationIconColor,
        'text-color': theme.directivesBicycleRepairStationTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bicycle_rental'],
        'icon-image': 'rental_bicycle',
        'icon-color': theme.directivesBicycleRentalIconColor,
        'text-color': theme.directivesBicycleRentalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'boat_rental'],
        'icon-image': 'boat_rental',
        'icon-color': theme.directivesBoatRentalIconColor,
        'text-color': theme.directivesBoatRentalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bus_station'],
        'icon-image': 'bus_station',
        'icon-color': theme.directivesBusStationIconColor,
        'text-color': theme.directivesBusStationTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'car_rental'],
        'icon-image': 'rental_car',
        'icon-color': theme.directivesCarRentalIconColor,
        'text-color': theme.directivesCarRentalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'car_wash'],
        'icon-image': 'car_wash',
        'icon-color': theme.directivesCarWashIconColor,
        'text-color': theme.directivesCarWashTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'vehicle_inspection'],
        'icon-image': 'vehicle_inspection',
        'icon-color': theme.directivesVehicleInspectionIconColor,
        'text-color': theme.directivesVehicleInspectionTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'charging_station'],
        'icon-image': 'charging_station',
        'icon-color': theme.directivesChargingStationIconColor,
        'text-color': theme.directivesChargingStationTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'ferry_terminal'],
        'icon-image': 'ferry',
        'icon-color': theme.directivesFerryTerminalIconColor,
        'text-color': theme.directivesFerryTerminalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'fuel'],
        'icon-image': 'fuel',
        'icon-color': theme.directivesFuelIconColor,
        'text-color': theme.directivesFuelTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'motorcycle_parking'],
        'icon-image': 'motorcycle_parking',
        'icon-color': theme.directivesMotorcycleParkingIconColor,
        'text-color': theme.directivesMotorcycleParkingTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'parking'],
        'icon-image': 'parking',
        'icon-color': theme.directivesParkingIconColor,
        'text-color': theme.directivesParkingTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'parking_entrance'],
        'icon-image': 'entrance',
        'icon-color': theme.directivesParkingEntranceIconColor,
        'text-color': theme.directivesParkingEntranceTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'taxi'],
        'icon-image': 'taxi',
        'icon-color': theme.directivesTaxiIconColor,
        'text-color': theme.directivesTaxiTextColor,
    },
    {
        filter: ['==', ['get', 'highway'], 'bus_stop'],
        'icon-image': 'bus_stop',
        'icon-color': 'rgb(0, 146, 219)',
        'text-color': 'rgb(0, 146, 219)',
    },

    // Amenity: financial
    {
        filter: ['==', ['get', 'amenity'], 'atm'],
        'icon-image': 'atm',
        'icon-color': theme.directivesAtmIconColor,
        'text-color': theme.directivesAtmTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bank'],
        'icon-image': 'bank',
        'icon-color': theme.directivesBankIconColor,
        'text-color': theme.directivesBankTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bureau_de_change'],
        'icon-image': 'bureau_de_change',
        'icon-color': theme.directivesBureauDeChangeIconColor,
        'text-color': theme.directivesBureauDeChangeTextColor,
    },

    // Amenity: healthcare
    {
        filter: ['==', ['get', 'amenity'], 'clinic'],
        'icon-image': 'hospital',
        'icon-color': theme.directivesCliniqueIconColor,
        'text-color': theme.directivesCliniqueTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'dentist'],
        'icon-image': 'dentist',
        'icon-color': theme.directivesDentistIconColor,
        'text-color': theme.directivesDentistTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'doctors'],
        'icon-image': 'doctors',
        'icon-color': theme.directivesDoctorIconColor,
        'text-color': theme.directivesDoctorTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'hospital'],
        'icon-image': 'hospital',
        'icon-color': theme.directivesHospitalIconColor,
        'text-color': theme.directivesHospitalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'nursing_home'],
        'icon-image': 'nursing_home',
        'icon-color': theme.directivesNursingHomeIconColor,
        'text-color': theme.directivesNursingHomeTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'pharmacy'],
        'icon-image': 'pharmacy',
        'icon-color': theme.directivesPharmacieIconColor,
        'text-color': theme.directivesPharmacieTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'social_facility'],
        'icon-image': 'social_facility',
        'icon-color': theme.directivesSocialFacilityIconColor,
        'text-color': theme.directivesSocialFacilityTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'veterinary'],
        'icon-image': 'veterinary',
        'icon-color': theme.directivesVeterinaryIconColor,
        'text-color': theme.directivesVeterinaryTextColor,
    },

    // Amenity: entertainment, arts & culture
    {
        filter: ['==', ['get', 'amenity'], 'arts_centre'],
        'icon-image': 'arts_centre',
        'icon-color': theme.directivesArtsCentreIconColor,
        'text-color': theme.directivesArtsCentreTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'casino'],
        'icon-image': 'casino',
        'icon-color': theme.directivesCasinoIconColor,
        'text-color': theme.directivesCasinoTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'cinema'],
        'icon-image': 'cinema',
        'icon-color': theme.directivesCinemaIconColor,
        'text-color': theme.directivesCinemaTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'community_centre'],
        'icon-image': 'community_centre',
        'icon-color': theme.directivesCommunityCentreIconColor,
        'text-color': theme.directivesCommunityCentreTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'fountain'],
        'icon-image': 'fountain',
        'icon-color': theme.directivesFountainIconColor,
        'text-color': theme.directivesFountainTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'nightclub'],
        'icon-image': 'nightclub',
        'icon-color': theme.directivesNigthclubIconColor,
        'text-color': theme.directivesNightclubTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'public_bookcase'],
        'icon-image': 'public_bookcase',
        'icon-color': theme.directivesPublicBookcaseIconColor,
        'text-color': theme.directivesPublicBookCaseTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'theatre'],
        'icon-image': 'theatre',
        'icon-color': theme.directivesTheatreIconColor,
        'text-color': theme.directivesTheatreTextColor,
    },

    // Amenity: public service
    {
        filter: ['==', ['get', 'amenity'], 'courthouse'],
        'icon-image': 'courthouse',
        'icon-color': theme.directivesCourthouseIconColor,
        'text-color': theme.directivesCourthouseTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'fire_station'],
        'icon-image': 'firestation',
        'icon-color': theme.directivesFireStationIconColor,
        'text-color': theme.directivesFireStationTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'police'],
        'icon-image': 'police',
        'icon-color': theme.directivesPoliceIconColor,
        'text-color': theme.directivesPoliceTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'post_box'],
        'icon-image': 'post_box',
        'icon-color': theme.directivesPostBoxIconColor,
        'text-color': theme.directivesPostBoxTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'post_office'],
        'icon-image': 'post_office',
        'icon-color': theme.directivesPostOfficeIconColor,
        'text-color': theme.directivesPostOfficeTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'prison'],
        'icon-image': 'prison',
        'icon-color': theme.directivesPrisonIconColor,
        'text-color': theme.directivesPrisonTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'townhall'],
        'icon-image': 'town_hall',
        'icon-color': theme.directivesTownhallIconColor,
        'text-color': theme.directivesTowmhallTextColor,
    },

    // Amenity: facilities
    {
        filter: ['==', ['get', 'amenity'], 'bbq'],
        'icon-image': 'bbq',
        'icon-color': theme.directivesBbqIconColor,
        'text-color': theme.directivesBbqTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bench'],
        'icon-image': 'bench',
        'icon-color': theme.directivesBenchIconColor,
        'text-color': theme.directivesBenchTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'drinking_water'],
        'icon-image': 'drinking_water',
        'icon-color': theme.directivesDrinkingWaterIconColor,
        'text-color': theme.directivesDrinkingWaterTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'shelter'],
        'icon-image': 'shelter',
        'icon-color': theme.directivesShelterIconColor,
        'text-color': theme.directivesShelterTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'shower'],
        'icon-image': 'shower',
        'icon-color': theme.directivesShowerIconColor,
        'text-color': theme.directivesShowerTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'telephone'],
        'icon-image': 'telephone',
        'icon-color': theme.directivesTelephoneIconColor,
        'text-color': theme.directivesTelephoneTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'toilets'],
        'icon-image': 'toilets',
        'icon-color': theme.directivesToiletsIconColor,
        'text-color': theme.directivesToiletsTextColor,
    },

    // Amenity: waste management
    {
        filter: ['==', ['get', 'amenity'], 'recycling'],
        'icon-image': 'recycling',
        'icon-color': theme.directivesRecyclingIconColor,
        'text-color': theme.directivesRecyclingTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'waste_basket'],
        'icon-image': 'waste_basket',
        'icon-color': theme.directivesWasteBasketIconColor,
        'text-color': theme.directivesWasteBasketTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'waste_disposal'],
        'icon-image': 'waste_disposal',
        'icon-color': theme.directivesWasteDisposalIconColor,
        'text-color': theme.directivesWasteDisposalTextColor,
    },

    // Amenity: Others
    {
        filter: ['==', ['get', 'amenity'], 'childcare'],
        'icon-image': 'place-6',
        'icon-color': theme.directivesChildcareIconColor,
        'text-color': theme.directivesChildcareTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'hunting_stand'],
        'icon-image': 'hunting_stand',
        'icon-color': theme.directivesHuntingStandIconColor,
        'text-color': theme.directivesHuntingStandTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'internet_cafe'],
        'icon-image': 'internet_cafe',
        'icon-color': theme.directivesInternetCafeIconColor,
        'text-color': theme.directivesInternetCafeTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'marketplace'],
        'icon-image': 'marketplace',
        'icon-color': theme.directivesMarketplaceIconColor,
        'text-color': theme.directivesMarketplaceTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'place_of_worship'],
        'icon-image': 'place_of_worship',
        'icon-color': theme.directivesPlaceOfWorkshipIconColor,
        'text-color': theme.directivesPlaceOfWorkshipTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'public_bath'],
        'icon-image': 'public_bath',
        'icon-color': theme.directivesPublicBathIconColor,
        'text-color': theme.directivesPublicBathTextColor,
    },

    // Historic
    {
        filter: ['==', ['get', 'historic'], 'archaeological_site'],
        'icon-image': 'archaeological_site',
        'icon-color': theme.directivesArchaeologicalSiteIconColor,
        'text-color': theme.directivesArchaeologicalSiteTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'castle'],
        'icon-image': 'castle',
        'icon-color': theme.directivesCastleIconColor,
        'text-color': theme.directivesCastleTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'city_gate'],
        'icon-image': 'city_gate',
        'icon-color': theme.directivesCityGateIconColor,
        'text-color': theme.directivesCityGateTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'fort'],
        'icon-image': 'fort',
        'icon-color': theme.directivesFortIconColor,
        'text-color': theme.directivesFortTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'manor'],
        'icon-image': 'manor',
        'icon-color': theme.directivesManorIconColor,
        'text-color': theme.directivesManorTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'memorial'],
        'icon-image': 'memorial',
        'icon-color': theme.directivesMemorialIconColor,
        'text-color': theme.directivesMemorialTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'monument'],
        'icon-image': 'monument',
        'icon-color': theme.directivesMonumentIconColor,
        'text-color': theme.directivesMonumentTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'wayside_cross'],
        'icon-image': 'wayside_cross',
        'icon-color': theme.directivesWaysideCrossIconColor,
        'text-color': theme.directivesWaysideCrossTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'wayside_shrine'],
        'icon-image': 'wayside_shrine',
        'icon-color': theme.directivesWayShrineIconColor,
        'text-color': theme.directivesWaysideShrineTextColor,
    },

    // Leisure
    {
        filter: ['==', ['get', 'leisure'], 'amusement_arcade'],
        'icon-image': 'amusement_arcade',
        'icon-color': theme.directivesAmusementArcadeIconColor,
        'text-color': theme.directivesAmusementArcadeTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'beach_resort'],
        'icon-image': 'beach_resort',
        'icon-color': theme.directivesBeachResortIconColor,
        'text-color': theme.directivesBeachResortTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'bird_hide'],
        'icon-image': 'bird_hide',
        'icon-color': theme.directivesBirdHideIconColor,
        'text-color': theme.directivesBirdHideTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'bowling_alley'],
        'icon-image': 'bowling_alley',
        'icon-color': theme.directivesBowlingAlleyIconColor,
        'text-color': theme.directivesBowlingAlleyTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'firepit'],
        'icon-image': 'firepit',
        'icon-color': theme.directivesFirepitIconColor,
        'text-color': theme.directivesFirepitTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'fishing'],
        'icon-image': 'fishing',
        'icon-color': theme.directivesFishingIconColor,
        'text-color': theme.directivesFishingTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'fitness_centre'],
        'icon-image': 'sports',
        'icon-color': theme.directivesFitnessCentreIconColor,
        'text-color': theme.directivesFitnessCentreTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'fitness_station'],
        'icon-image': 'sports',
        'icon-color': theme.directivesFitnessStationIconColor,
        'text-color': theme.directivesFitnessStationTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'golf_course'],
        'icon-image': 'golf_course',
        'icon-color': theme.directivesGolfCourseIconColor,
        'text-color': theme.directivesGolfCourseTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'miniature_golf'],
        'icon-image': 'miniature_golf',
        'icon-color': theme.directivesMiniatureGolfIconColor,
        'text-color': theme.directivesMiniatureGolfTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'outdoor_seating'],
        'icon-image': 'outdoor_seating',
        'icon-color': theme.directivesOutdoorSeatingIconColor,
        'text-color': theme.directivesOutdoorSeatingTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'picnic_table'],
        'icon-image': 'picnic',
        'icon-color': theme.directivesPicnicTableIconColor,
        'text-color': theme.directivesPicnicTableTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'playground'],
        'icon-image': 'playground',
        'icon-color': theme.directivesPlaygroundIconColor,
        'text-color': theme.directivesPlaygroundTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'sauna'],
        'icon-image': 'sauna',
        'icon-color': theme.directivesSaunaIconColor,
        'text-color': theme.directivesSaunaTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'slipway'],
        'icon-image': 'slipway',
        'icon-color': theme.directivesSlipwayIconColor,
        'text-color': theme.directivesSlipwayTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'swimming_area'],
        'icon-image': 'swimming_area',
        'icon-color': theme.directivesSwimmingAreaIconColor,
        'text-color': theme.directivesSwimmingAreaTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'water_park'],
        'icon-image': 'water_park',
        'icon-color': theme.directivesWaterParkIconColor,
        'text-color': theme.directivesWaterParkTextColor,
    },

    // Man-made
    {
        filter: ['==', ['get', 'man_made'], 'chimney'],
        'icon-image': 'chimney',
        'icon-color': theme.directivesChimneyIconColor,
        'text-color': theme.directivesChimneyTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'communications_tower'],
        'icon-image': 'communications_tower',
        'icon-color': theme.directivesCommunicationTowerIconColor,
        'text-color': theme.directivesCommunicationTowerTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'crane'],
        'icon-image': 'crane',
        'icon-color': theme.directivesCraneIconColor,
        'text-color': theme.directivesCraneTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'cross'],
        'icon-image': 'cross',
        'icon-color': theme.directivesCrossIconColor,
        'text-color': theme.directivesCrossTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'lighthouse'],
        'icon-image': 'lighthouse',
        'icon-color': theme.directivesLighthouseIconColor,
        'text-color': theme.directivesLightHouseTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'mast'],
        'icon-image': 'mast',
        'icon-color': theme.directivesMastIconColor,
        'text-color': theme.directivesMastTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'obelisk'],
        'icon-image': 'obelisk',
        'icon-color': theme.directivesObeliskIconColor,
        'text-color': theme.directivesObeliskTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'silo'],
        'icon-image': 'silo',
        'icon-color': theme.directivesSiloIconColor,
        'text-color': theme.directivesSiloTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'storage_tank'],
        'icon-image': 'storage_tank',
        'icon-color': theme.directivesStorageTankIconColor,
        'text-color': theme.directivesStorageTankTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'telescope'],
        'icon-image': 'telescope',
        'icon-color': theme.directivesTelescopeIconColor,
        'text-color': theme.directivesTelescopeTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'tower'],
        'icon-image': 'tower_generic',
        'icon-color': theme.directivesTowerIconColor,
        'text-color': theme.directivesTowerTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'water_tower'],
        'icon-image': 'water_tower',
        'icon-color': theme.directivesWaterTowerIconColor,
        'text-color': theme.directivesWaterTowerTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'windmill'],
        'icon-image': 'windmill',
        'icon-color': theme.directivesWindmillIconColor,
        'text-color': theme.directivesWindmillTextColor,
    },

    // Military
    {
        filter: ['==', ['get', 'military'], 'bunker'],
        'icon-image': 'bunker',
        'icon-color': theme.directivesBunkerIconColor,
        'text-color': theme.directivesBunkerTextColor,
    },

    // Natural
    {
        filter: ['==', ['get', 'natural'], 'spring'],
        'icon-image': 'spring',
        'icon-color': theme.directivesSpringIconColor,
        'text-color': theme.directivesSpringTextColor,
    },
    {
        filter: ['==', ['get', 'natural'], 'cave_entrance'],
        'icon-image': 'entrance',
        'icon-color': theme.directivesCaveEntranceIconColor,
        'text-color': theme.directivesCaveEntranceTextColor,
    },
    {
        filter: ['==', ['get', 'natural'], 'peak'],
        'icon-image': 'peak',
        'icon-color': theme.directivesPeakIconColor,
        'text-color': theme.directivesPeakTextColor,
    },
    {
        filter: ['==', ['get', 'natural'], 'saddle'],
        'icon-image': 'saddle',
        'icon-color': theme.directivesSaddleIconColor,
        'text-color': theme.directivesSaddleTextColor,
    },
    {
        filter: ['==', ['get', 'natural'], 'volcano'],
        'icon-image': 'volcano',
        'icon-color': theme.directivesVolcanoIconColor,
        'text-color': theme.directivesVolcanoTextColor,
    },

    // Railway: stations and stops
    {
        filter: ['==', ['get', 'railway'], 'halt'],
        'icon-image': 'place-6',
        'icon-color': theme.directivesHaltIconColor,
        'text-color': theme.directivesHaltTextColor,
    },
    {
        filter: ['==', ['get', 'railway'], 'station'],
        'icon-image': 'place-6',
        'icon-color': theme.directivesStationIconColor,
        'text-color': theme.directivesStationTextColor,
    },
    {
        filter: ['==', ['get', 'railway'], 'subway_entrance'],
        'icon-image': 'entrance',
        'icon-color': theme.directivesSubwayEntranceIconColor,
        'text-color': theme.directivesSubwayEntranceTextColor,
    },
    {
        filter: ['==', ['get', 'railway'], 'tram_stop'],
        'icon-image': 'tram_stop',
        'icon-color': theme.directivesTramStopIconColor,
        'text-color': theme.directivesTramStopTextColor,
    },

    // Railway: other railways
    {
        filter: ['==', ['get', 'railway'], 'crossing'],
        'icon-image': 'level_crossing',
        'icon-color': theme.directivesCrossingIconColor,
        'text-color': theme.directivesCrossingTextColor,
    },
    {
        filter: ['==', ['get', 'railway'], 'level_crossing'],
        'icon-image': 'level_crossing',
        'icon-color': theme.directivesLevelCrossingIconColor,
        'text-color': theme.directivesLevelCrossingTextColor,
    },

    // Tourism
    {
        filter: ['==', ['get', 'tourism'], 'alpine_hut'],
        'icon-image': 'alpine_hut',
        'icon-color': theme.directivesAlpineHutIconColor,
        'text-color': theme.directivesAlpineHutTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'apartment'],
        'icon-image': 'apartment',
        'icon-color': theme.directivesApartmentIconColor,
        'text-color': theme.directivesApartmentTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'artwork'],
        'icon-image': 'artwork',
        'icon-color': theme.directivesArtworkIconColor,
        'text-color': theme.directivesArtworkTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'camp_site'],
        'icon-image': 'camping',
        'icon-color': theme.directivesCampSiteIconColor,
        'text-color': theme.directivesCampSiteTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'caravan_site'],
        'icon-image': 'caravan_park',
        'icon-color': theme.directivesCaravaneSiteIconColor,
        'text-color': theme.directivesCaravaneSiteTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'chalet'],
        'icon-image': 'chalet',
        'icon-color': theme.directivesChaletIconColor,
        'text-color': theme.directivesChaletTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'gallery'],
        'icon-image': 'art',
        'icon-color': theme.directivesGalleryIconColor,
        'text-color': theme.directivesGalleryTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'guest_house'],
        'icon-image': 'guest_house',
        'icon-color': theme.directivesGuestHouseIconColor,
        'text-color': theme.directivesGuestHouseextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'hostel'],
        'icon-image': 'hostel',
        'icon-color': theme.directivesHostelIconColor,
        'text-color': theme.directivesHostelTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'motel'],
        'icon-image': 'motel',
        'icon-color': theme.directivesMotelIconColor,
        'text-color': theme.directivesMotelTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'museum'],
        'icon-image': 'museum',
        'icon-color': theme.directivesMuseumIconColor,
        'text-color': theme.directivesMuseumTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'picnic_site'],
        'icon-image': 'picnic',
        'icon-color': theme.directivesPicnicSiteIconColor,
        'text-color': theme.directivesPicnicSiteTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'viewpoint'],
        'icon-image': 'viewpoint',
        'icon-color': theme.directivesViewpointIconColor,
        'text-color': theme.directivesViewpointTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'wilderness_hut'],
        'icon-image': 'wilderness_hut',
        'icon-color': theme.directivesWildernessHutIconColor,
        'text-color': theme.directivesWildernessHutTextColor,
    },

    // Waterway: barriers on waterways
    {
        filter: ['==', ['get', 'waterway'], 'dam'],
        'icon-image': 'dam',
        'icon-color': theme.directivesDamIconColor,
        'text-color': theme.directivesDamTextColor,
    },
    {
        filter: ['==', ['get', 'waterway'], 'weir'],
        'icon-image': 'weir',
        'icon-color': theme.directivesWeirdIconColor,
        'text-color': theme.directivesWeirdTextColor,
    },
    {
        filter: ['==', ['get', 'waterway'], 'waterfall'],
        'icon-image': 'waterfall',
        'icon-color': theme.directivesWaterfallIconColor,
        'text-color': theme.directivesWaterfallTextColor,
    },
    {
        filter: ['==', ['get', 'waterway'], 'lock_gate'],
        'icon-image': 'lock_gate',
        'icon-color': theme.directivesLockGateIconColor,
        'text-color': theme.directivesLockGateTextColor,
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
        'icon-halo-color': theme.iconsIconHaloColor,
        'icon-halo-width': 1,
        'text-halo-width': 1,
        'text-halo-color': theme.iconsTextHaloColor,
    },
});
