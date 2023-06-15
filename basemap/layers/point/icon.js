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
        'icon-color': theme.pointIconBarIconColor,
        'text-color': theme.pointIconBarTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'biergarten'],
        'icon-image': 'biergarten',
        'icon-color': theme.pointIconBiergartenIconColor,
        'text-color': theme.pointIconBiergartenTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'cafe'],
        'icon-image': 'cafe',
        'icon-color': theme.pointIconCafeIconColor,
        'text-color': theme.pointIconCafeTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'fast_food'],
        'icon-image': 'fast_food',
        'icon-color': theme.pointIconFastFoodIconColor,
        'text-color': theme.pointIconFastFoodTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'food_court'],
        'icon-image': 'food_court',
        'icon-color': theme.pointIconFoodCourtIconColor,
        'text-color': theme.pointIconFoodCourtTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'ice_cream'],
        'icon-image': 'ice_cream',
        'icon-color': theme.pointIconIceCreamIconColor,
        'text-color': theme.pointIconIceCreamTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'pub'],
        'icon-image': 'pub',
        'icon-color': theme.pointIconPubIconColor,
        'text-color': theme.pointIconPubTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'restaurant'],
        'icon-image': 'restaurant',
        'icon-color': theme.pointIconRestaurantIconColor,
        'text-color': theme.pointIconRestaurantTextColor,
    },

    // Amenity: education
    // {
    //     filter: ['==', ['get', 'amenity'], 'driving_school'],
    //     'icon-image': 'driving_school',
    //     'icon-color': colorScheme.pointIconDrivingSchoolIconColor,
    //     'text-color': colorScheme.pointIconDrivingSchoolTextColor,
    // },
    {
        filter: ['==', ['get', 'amenity'], 'library'],
        'icon-image': 'library',
        'icon-color': theme.pointIconLibraryIconColor,
        'text-color': theme.pointIconLibraryTextColor,
    },

    // Amenity: transportation
    {
        filter: ['==', ['get', 'amenity'], 'bicycle_parking'],
        'icon-image': 'bicycle_parking',
        'icon-color': theme.pointIconBicycleParkingIconColor,
        'text-color': theme.pointIconBicycleParkingTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bicycle_repair_station'],
        'icon-image': 'bicycle_repair_station',
        'icon-color': theme.pointIconBicycleRepairStationIconColor,
        'text-color': theme.pointIconBicycleRepairStationTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bicycle_rental'],
        'icon-image': 'rental_bicycle',
        'icon-color': theme.pointIconBicycleRentalIconColor,
        'text-color': theme.pointIconBicycleRentalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'boat_rental'],
        'icon-image': 'boat_rental',
        'icon-color': theme.pointIconBoatRentalIconColor,
        'text-color': theme.pointIconBoatRentalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bus_station'],
        'icon-image': 'bus_station',
        'icon-color': theme.pointIconBusStationIconColor,
        'text-color': theme.pointIconBusStationTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'car_rental'],
        'icon-image': 'rental_car',
        'icon-color': theme.pointIconCarRentalIconColor,
        'text-color': theme.pointIconCarRentalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'car_wash'],
        'icon-image': 'car_wash',
        'icon-color': theme.pointIconCarWashIconColor,
        'text-color': theme.pointIconCarWashTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'vehicle_inspection'],
        'icon-image': 'vehicle_inspection',
        'icon-color': theme.pointIconVehicleInspectionIconColor,
        'text-color': theme.pointIconVehicleInspectionTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'charging_station'],
        'icon-image': 'charging_station',
        'icon-color': theme.pointIconChargingStationIconColor,
        'text-color': theme.pointIconChargingStationTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'ferry_terminal'],
        'icon-image': 'ferry',
        'icon-color': theme.pointIconFerryTerminalIconColor,
        'text-color': theme.pointIconFerryTerminalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'fuel'],
        'icon-image': 'fuel',
        'icon-color': theme.pointIconFuelIconColor,
        'text-color': theme.pointIconFuelTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'motorcycle_parking'],
        'icon-image': 'motorcycle_parking',
        'icon-color': theme.pointIconMotorcycleParkingIconColor,
        'text-color': theme.pointIconMotorcycleParkingTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'parking'],
        'icon-image': 'parking',
        'icon-color': theme.pointIconParkingIconColor,
        'text-color': theme.pointIconParkingTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'parking_entrance'],
        'icon-image': 'entrance',
        'icon-color': theme.pointIconParkingEntranceIconColor,
        'text-color': theme.pointIconParkingEntranceTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'taxi'],
        'icon-image': 'taxi',
        'icon-color': theme.pointIconTaxiIconColor,
        'text-color': theme.pointIconTaxiTextColor,
    },
    {
        filter: ['==', ['get', 'highway'], 'bus_stop'],
        'icon-image': 'bus_stop',
        'icon-color': theme.pointIconBusStopIconColor,
        'text-color': theme.pointIconBusStopTextColo,
    },

    // Amenity: financial
    {
        filter: ['==', ['get', 'amenity'], 'atm'],
        'icon-image': 'atm',
        'icon-color': theme.pointIconAtmIconColor,
        'text-color': theme.pointIconAtmTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bank'],
        'icon-image': 'bank',
        'icon-color': theme.pointIconBankIconColor,
        'text-color': theme.pointIconBankTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bureau_de_change'],
        'icon-image': 'bureau_de_change',
        'icon-color': theme.pointIconBureauDeChangeIconColor,
        'text-color': theme.pointIconBureauDeChangeTextColor,
    },

    // Amenity: healthcare
    {
        filter: ['==', ['get', 'amenity'], 'clinic'],
        'icon-image': 'hospital',
        'icon-color': theme.pointIconCliniqueIconColor,
        'text-color': theme.pointIconCliniqueTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'dentist'],
        'icon-image': 'dentist',
        'icon-color': theme.pointIconDentistIconColor,
        'text-color': theme.pointIconDentistTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'doctors'],
        'icon-image': 'doctors',
        'icon-color': theme.pointIconDoctorIconColor,
        'text-color': theme.pointIconDoctorTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'hospital'],
        'icon-image': 'hospital',
        'icon-color': theme.pointIconHospitalIconColor,
        'text-color': theme.pointIconHospitalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'nursing_home'],
        'icon-image': 'nursing_home',
        'icon-color': theme.pointIconNursingHomeIconColor,
        'text-color': theme.pointIconNursingHomeTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'pharmacy'],
        'icon-image': 'pharmacy',
        'icon-color': theme.pointIconPharmacieIconColor,
        'text-color': theme.pointIconPharmacieTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'social_facility'],
        'icon-image': 'social_facility',
        'icon-color': theme.pointIconSocialFacilityIconColor,
        'text-color': theme.pointIconSocialFacilityTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'veterinary'],
        'icon-image': 'veterinary',
        'icon-color': theme.pointIconVeterinaryIconColor,
        'text-color': theme.pointIconVeterinaryTextColor,
    },

    // Amenity: entertainment, arts & culture
    {
        filter: ['==', ['get', 'amenity'], 'arts_centre'],
        'icon-image': 'arts_centre',
        'icon-color': theme.pointIconArtsCentreIconColor,
        'text-color': theme.pointIconArtsCentreTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'casino'],
        'icon-image': 'casino',
        'icon-color': theme.pointIconCasinoIconColor,
        'text-color': theme.pointIconCasinoTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'cinema'],
        'icon-image': 'cinema',
        'icon-color': theme.pointIconCinemaIconColor,
        'text-color': theme.pointIconCinemaTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'community_centre'],
        'icon-image': 'community_centre',
        'icon-color': theme.pointIconCommunityCentreIconColor,
        'text-color': theme.pointIconCommunityCentreTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'fountain'],
        'icon-image': 'fountain',
        'icon-color': theme.pointIconFountainIconColor,
        'text-color': theme.pointIconFountainTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'nightclub'],
        'icon-image': 'nightclub',
        'icon-color': theme.pointIconNigthclubIconColor,
        'text-color': theme.pointIconNightclubTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'public_bookcase'],
        'icon-image': 'public_bookcase',
        'icon-color': theme.pointIconPublicBookcaseIconColor,
        'text-color': theme.pointIconPublicBookCaseTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'theatre'],
        'icon-image': 'theatre',
        'icon-color': theme.pointIconTheatreIconColor,
        'text-color': theme.pointIconTheatreTextColor,
    },

    // Amenity: public service
    {
        filter: ['==', ['get', 'amenity'], 'courthouse'],
        'icon-image': 'courthouse',
        'icon-color': theme.pointIconCourthouseIconColor,
        'text-color': theme.pointIconCourthouseTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'fire_station'],
        'icon-image': 'firestation',
        'icon-color': theme.pointIconFireStationIconColor,
        'text-color': theme.pointIconFireStationTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'police'],
        'icon-image': 'police',
        'icon-color': theme.pointIconPoliceIconColor,
        'text-color': theme.pointIconPoliceTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'post_box'],
        'icon-image': 'post_box',
        'icon-color': theme.pointIconPostBoxIconColor,
        'text-color': theme.pointIconPostBoxTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'post_office'],
        'icon-image': 'post_office',
        'icon-color': theme.pointIconPostOfficeIconColor,
        'text-color': theme.pointIconPostOfficeTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'prison'],
        'icon-image': 'prison',
        'icon-color': theme.pointIconPrisonIconColor,
        'text-color': theme.pointIconPrisonTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'townhall'],
        'icon-image': 'town_hall',
        'icon-color': theme.pointIconTownhallIconColor,
        'text-color': theme.pointIconTowmhallTextColor,
    },

    // Amenity: facilities
    {
        filter: ['==', ['get', 'amenity'], 'bbq'],
        'icon-image': 'bbq',
        'icon-color': theme.pointIconBbqIconColor,
        'text-color': theme.pointIconBbqTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bench'],
        'icon-image': 'bench',
        'icon-color': theme.pointIconBenchIconColor,
        'text-color': theme.pointIconBenchTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'drinking_water'],
        'icon-image': 'drinking_water',
        'icon-color': theme.pointIconDrinkingWaterIconColor,
        'text-color': theme.pointIconDrinkingWaterTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'shelter'],
        'icon-image': 'shelter',
        'icon-color': theme.pointIconShelterIconColor,
        'text-color': theme.pointIconShelterTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'shower'],
        'icon-image': 'shower',
        'icon-color': theme.pointIconShowerIconColor,
        'text-color': theme.pointIconShowerTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'telephone'],
        'icon-image': 'telephone',
        'icon-color': theme.pointIconTelephoneIconColor,
        'text-color': theme.pointIconTelephoneTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'toilets'],
        'icon-image': 'toilets',
        'icon-color': theme.pointIconToiletsIconColor,
        'text-color': theme.pointIconToiletsTextColor,
    },

    // Amenity: waste management
    {
        filter: ['==', ['get', 'amenity'], 'recycling'],
        'icon-image': 'recycling',
        'icon-color': theme.pointIconRecyclingIconColor,
        'text-color': theme.pointIconRecyclingTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'waste_basket'],
        'icon-image': 'waste_basket',
        'icon-color': theme.pointIconWasteBasketIconColor,
        'text-color': theme.pointIconWasteBasketTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'waste_disposal'],
        'icon-image': 'waste_disposal',
        'icon-color': theme.pointIconWasteDisposalIconColor,
        'text-color': theme.pointIconWasteDisposalTextColor,
    },

    // Amenity: Others
    {
        filter: ['==', ['get', 'amenity'], 'childcare'],
        'icon-image': 'place-6',
        'icon-color': theme.pointIconChildcareIconColor,
        'text-color': theme.pointIconChildcareTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'hunting_stand'],
        'icon-image': 'hunting_stand',
        'icon-color': theme.pointIconHuntingStandIconColor,
        'text-color': theme.pointIconHuntingStandTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'internet_cafe'],
        'icon-image': 'internet_cafe',
        'icon-color': theme.pointIconInternetCafeIconColor,
        'text-color': theme.pointIconInternetCafeTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'marketplace'],
        'icon-image': 'marketplace',
        'icon-color': theme.pointIconMarketplaceIconColor,
        'text-color': theme.pointIconMarketplaceTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'place_of_worship'],
        'icon-image': 'place_of_worship',
        'icon-color': theme.pointIconPlaceOfWorkshipIconColor,
        'text-color': theme.pointIconPlaceOfWorkshipTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'public_bath'],
        'icon-image': 'public_bath',
        'icon-color': theme.pointIconPublicBathIconColor,
        'text-color': theme.pointIconPublicBathTextColor,
    },

    // Historic
    {
        filter: ['==', ['get', 'historic'], 'archaeological_site'],
        'icon-image': 'archaeological_site',
        'icon-color': theme.pointIconArchaeologicalSiteIconColor,
        'text-color': theme.pointIconArchaeologicalSiteTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'castle'],
        'icon-image': 'castle',
        'icon-color': theme.pointIconCastleIconColor,
        'text-color': theme.pointIconCastleTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'city_gate'],
        'icon-image': 'city_gate',
        'icon-color': theme.pointIconCityGateIconColor,
        'text-color': theme.pointIconCityGateTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'fort'],
        'icon-image': 'fort',
        'icon-color': theme.pointIconFortIconColor,
        'text-color': theme.pointIconFortTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'manor'],
        'icon-image': 'manor',
        'icon-color': theme.pointIconManorIconColor,
        'text-color': theme.pointIconManorTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'memorial'],
        'icon-image': 'memorial',
        'icon-color': theme.pointIconMemorialIconColor,
        'text-color': theme.pointIconMemorialTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'monument'],
        'icon-image': 'monument',
        'icon-color': theme.pointIconMonumentIconColor,
        'text-color': theme.pointIconMonumentTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'wayside_cross'],
        'icon-image': 'wayside_cross',
        'icon-color': theme.pointIconWaysideCrossIconColor,
        'text-color': theme.pointIconWaysideCrossTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'wayside_shrine'],
        'icon-image': 'wayside_shrine',
        'icon-color': theme.pointIconWayShrineIconColor,
        'text-color': theme.pointIconWaysideShrineTextColor,
    },

    // Leisure
    {
        filter: ['==', ['get', 'leisure'], 'amusement_arcade'],
        'icon-image': 'amusement_arcade',
        'icon-color': theme.pointIconAmusementArcadeIconColor,
        'text-color': theme.pointIconAmusementArcadeTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'beach_resort'],
        'icon-image': 'beach_resort',
        'icon-color': theme.pointIconBeachResortIconColor,
        'text-color': theme.pointIconBeachResortTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'bird_hide'],
        'icon-image': 'bird_hide',
        'icon-color': theme.pointIconBirdHideIconColor,
        'text-color': theme.pointIconBirdHideTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'bowling_alley'],
        'icon-image': 'bowling_alley',
        'icon-color': theme.pointIconBowlingAlleyIconColor,
        'text-color': theme.pointIconBowlingAlleyTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'firepit'],
        'icon-image': 'firepit',
        'icon-color': theme.pointIconFirepitIconColor,
        'text-color': theme.pointIconFirepitTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'fishing'],
        'icon-image': 'fishing',
        'icon-color': theme.pointIconFishingIconColor,
        'text-color': theme.pointIconFishingTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'fitness_centre'],
        'icon-image': 'sports',
        'icon-color': theme.pointIconFitnessCentreIconColor,
        'text-color': theme.pointIconFitnessCentreTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'fitness_station'],
        'icon-image': 'sports',
        'icon-color': theme.pointIconFitnessStationIconColor,
        'text-color': theme.pointIconFitnessStationTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'golf_course'],
        'icon-image': 'golf_course',
        'icon-color': theme.pointIconGolfCourseIconColor,
        'text-color': theme.pointIconGolfCourseTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'miniature_golf'],
        'icon-image': 'miniature_golf',
        'icon-color': theme.pointIconMiniatureGolfIconColor,
        'text-color': theme.pointIconMiniatureGolfTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'outdoor_seating'],
        'icon-image': 'outdoor_seating',
        'icon-color': theme.pointIconOutdoorSeatingIconColor,
        'text-color': theme.pointIconOutdoorSeatingTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'picnic_table'],
        'icon-image': 'picnic',
        'icon-color': theme.pointIconPicnicTableIconColor,
        'text-color': theme.pointIconPicnicTableTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'playground'],
        'icon-image': 'playground',
        'icon-color': theme.pointIconPlaygroundIconColor,
        'text-color': theme.pointIconPlaygroundTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'sauna'],
        'icon-image': 'sauna',
        'icon-color': theme.pointIconSaunaIconColor,
        'text-color': theme.pointIconSaunaTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'slipway'],
        'icon-image': 'slipway',
        'icon-color': theme.pointIconSlipwayIconColor,
        'text-color': theme.pointIconSlipwayTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'swimming_area'],
        'icon-image': 'swimming_area',
        'icon-color': theme.pointIconSwimmingAreaIconColor,
        'text-color': theme.pointIconSwimmingAreaTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'water_park'],
        'icon-image': 'water_park',
        'icon-color': theme.pointIconWaterParkIconColor,
        'text-color': theme.pointIconWaterParkTextColor,
    },

    // Man-made
    {
        filter: ['==', ['get', 'man_made'], 'chimney'],
        'icon-image': 'chimney',
        'icon-color': theme.pointIconChimneyIconColor,
        'text-color': theme.pointIconChimneyTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'communications_tower'],
        'icon-image': 'communications_tower',
        'icon-color': theme.pointIconCommunicationTowerIconColor,
        'text-color': theme.pointIconCommunicationTowerTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'crane'],
        'icon-image': 'crane',
        'icon-color': theme.pointIconCraneIconColor,
        'text-color': theme.pointIconCraneTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'cross'],
        'icon-image': 'cross',
        'icon-color': theme.pointIconCrossIconColor,
        'text-color': theme.pointIconCrossTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'lighthouse'],
        'icon-image': 'lighthouse',
        'icon-color': theme.pointIconLighthouseIconColor,
        'text-color': theme.pointIconLightHouseTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'mast'],
        'icon-image': 'mast',
        'icon-color': theme.pointIconMastIconColor,
        'text-color': theme.pointIconMastTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'obelisk'],
        'icon-image': 'obelisk',
        'icon-color': theme.pointIconObeliskIconColor,
        'text-color': theme.pointIconObeliskTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'silo'],
        'icon-image': 'silo',
        'icon-color': theme.pointIconSiloIconColor,
        'text-color': theme.pointIconSiloTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'storage_tank'],
        'icon-image': 'storage_tank',
        'icon-color': theme.pointIconStorageTankIconColor,
        'text-color': theme.pointIconStorageTankTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'telescope'],
        'icon-image': 'telescope',
        'icon-color': theme.pointIconTelescopeIconColor,
        'text-color': theme.pointIconTelescopeTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'tower'],
        'icon-image': 'tower_generic',
        'icon-color': theme.pointIconTowerIconColor,
        'text-color': theme.pointIconTowerTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'water_tower'],
        'icon-image': 'water_tower',
        'icon-color': theme.pointIconWaterTowerIconColor,
        'text-color': theme.pointIconWaterTowerTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'windmill'],
        'icon-image': 'windmill',
        'icon-color': theme.pointIconWindmillIconColor,
        'text-color': theme.pointIconWindmillTextColor,
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
