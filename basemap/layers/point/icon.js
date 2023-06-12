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
    // Amenity: sustenance
    {
        filter: ['==', ['get', 'amenity'], 'bar'],
        'icon-image': 'bar',
        'icon-color': colorScheme.pointIconBarIconColor,
        'text-color': colorScheme.pointIconBarTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'biergarten'],
        'icon-image': 'biergarten',
        'icon-color': colorScheme.pointIconBiergartenIconColor,
        'text-color': colorScheme.pointIconBiergartenTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'cafe'],
        'icon-image': 'cafe',
        'icon-color': colorScheme.pointIconCafeIconColor,
        'text-color': colorScheme.pointIconCafeTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'fast_food'],
        'icon-image': 'fast_food',
        'icon-color': colorScheme.pointIconFastFoodIconColor,
        'text-color': colorScheme.pointIconFastFoodTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'food_court'],
        'icon-image': 'food_court',
        'icon-color': colorScheme.pointIconFoodCourtIconColor,
        'text-color': colorScheme.pointIconFoodCourtTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'ice_cream'],
        'icon-image': 'ice_cream',
        'icon-color': colorScheme.pointIconIceCreamIconColor,
        'text-color': colorScheme.pointIconIceCreamTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'pub'],
        'icon-image': 'pub',
        'icon-color': colorScheme.pointIconPubIconColor,
        'text-color': colorScheme.pointIconPubTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'restaurant'],
        'icon-image': 'restaurant',
        'icon-color': colorScheme.pointIconRestaurantIconColor,
        'text-color': colorScheme.pointIconRestaurantTextColor,
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
        'icon-color': colorScheme.pointIconLibraryIconColor,
        'text-color': colorScheme.pointIconLibraryTextColor,
    },

    // Amenity: transportation
    {
        filter: ['==', ['get', 'amenity'], 'bicycle_parking'],
        'icon-image': 'bicycle_parking',
        'icon-color': colorScheme.pointIconBicycleParkingIconColor,
        'text-color': colorScheme.pointIconBicycleParkingTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bicycle_repair_station'],
        'icon-image': 'bicycle_repair_station',
        'icon-color': colorScheme.pointIconBicycleRepairStationIconColor,
        'text-color': colorScheme.pointIconBicycleRepairStationTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bicycle_rental'],
        'icon-image': 'rental_bicycle',
        'icon-color': colorScheme.pointIconBicycleRentalIconColor,
        'text-color': colorScheme.pointIconBicycleRentalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'boat_rental'],
        'icon-image': 'boat_rental',
        'icon-color': colorScheme.pointIconBoatRentalIconColor,
        'text-color': colorScheme.pointIconBoatRentalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bus_station'],
        'icon-image': 'bus_station',
        'icon-color': colorScheme.pointIconBusStationIconColor,
        'text-color': colorScheme.pointIconBusStationTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'car_rental'],
        'icon-image': 'rental_car',
        'icon-color': colorScheme.pointIconCarRentalIconColor,
        'text-color': colorScheme.pointIconCarRentalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'car_wash'],
        'icon-image': 'car_wash',
        'icon-color': colorScheme.pointIconCarWashIconColor,
        'text-color': colorScheme.pointIconCarWashTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'vehicle_inspection'],
        'icon-image': 'vehicle_inspection',
        'icon-color': colorScheme.pointIconVehicleInspectionIconColor,
        'text-color': colorScheme.pointIconVehicleInspectionTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'charging_station'],
        'icon-image': 'charging_station',
        'icon-color': colorScheme.pointIconChargingStationIconColor,
        'text-color': colorScheme.pointIconChargingStationTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'ferry_terminal'],
        'icon-image': 'ferry',
        'icon-color': colorScheme.pointIconFerryTerminalIconColor,
        'text-color': colorScheme.pointIconFerryTerminalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'fuel'],
        'icon-image': 'fuel',
        'icon-color': colorScheme.pointIconFuelIconColor,
        'text-color': colorScheme.pointIconFuelTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'motorcycle_parking'],
        'icon-image': 'motorcycle_parking',
        'icon-color': colorScheme.pointIconMotorcycleParkingIconColor,
        'text-color': colorScheme.pointIconMotorcycleParkingTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'parking'],
        'icon-image': 'parking',
        'icon-color': colorScheme.pointIconParkingIconColor,
        'text-color': colorScheme.pointIconParkingTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'parking_entrance'],
        'icon-image': 'entrance',
        'icon-color': colorScheme.pointIconParkingEntranceIconColor,
        'text-color': colorScheme.pointIconParkingEntranceTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'taxi'],
        'icon-image': 'taxi',
        'icon-color': colorScheme.pointIconTaxiIconColor,
        'text-color': colorScheme.pointIconTaxiTextColor,
    },
    {
        filter: ['==', ['get', 'highway'], 'bus_stop'],
        'icon-image': 'bus_stop',
        'icon-color': colorScheme.pointIconBusStopIconColor,
        'text-color': colorScheme.pointIconBusStopTextColo,
    },

    // Amenity: financial
    {
        filter: ['==', ['get', 'amenity'], 'atm'],
        'icon-image': 'atm',
        'icon-color': colorScheme.pointIconAtmIconColor,
        'text-color': colorScheme.pointIconAtmTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bank'],
        'icon-image': 'bank',
        'icon-color': colorScheme.pointIconBankIconColor,
        'text-color': colorScheme.pointIconBankTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bureau_de_change'],
        'icon-image': 'bureau_de_change',
        'icon-color': colorScheme.pointIconBureauDeChangeIconColor,
        'text-color': colorScheme.pointIconBureauDeChangeTextColor,
    },

    // Amenity: healthcare
    {
        filter: ['==', ['get', 'amenity'], 'clinic'],
        'icon-image': 'hospital',
        'icon-color': colorScheme.pointIconCliniqueIconColor,
        'text-color': colorScheme.pointIconCliniqueTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'dentist'],
        'icon-image': 'dentist',
        'icon-color': colorScheme.pointIconDentistIconColor,
        'text-color': colorScheme.pointIconDentistTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'doctors'],
        'icon-image': 'doctors',
        'icon-color': colorScheme.pointIconDoctorIconColor,
        'text-color': colorScheme.pointIconDoctorTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'hospital'],
        'icon-image': 'hospital',
        'icon-color': colorScheme.pointIconHospitalIconColor,
        'text-color': colorScheme.pointIconHospitalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'nursing_home'],
        'icon-image': 'nursing_home',
        'icon-color': colorScheme.pointIconNursingHomeIconColor,
        'text-color': colorScheme.pointIconNursingHomeTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'pharmacy'],
        'icon-image': 'pharmacy',
        'icon-color': colorScheme.pointIconPharmacieIconColor,
        'text-color': colorScheme.pointIconPharmacieTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'social_facility'],
        'icon-image': 'social_facility',
        'icon-color': colorScheme.pointIconSocialFacilityIconColor,
        'text-color': colorScheme.pointIconSocialFacilityTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'veterinary'],
        'icon-image': 'veterinary',
        'icon-color': colorScheme.pointIconVeterinaryIconColor,
        'text-color': colorScheme.pointIconVeterinaryTextColor,
    },

    // Amenity: entertainment, arts & culture
    {
        filter: ['==', ['get', 'amenity'], 'arts_centre'],
        'icon-image': 'arts_centre',
        'icon-color': colorScheme.pointIconArtsCentreIconColor,
        'text-color': colorScheme.pointIconArtsCentreTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'casino'],
        'icon-image': 'casino',
        'icon-color': colorScheme.pointIconCasinoIconColor,
        'text-color': colorScheme.pointIconCasinoTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'cinema'],
        'icon-image': 'cinema',
        'icon-color': colorScheme.pointIconCinemaIconColor,
        'text-color': colorScheme.pointIconCinemaTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'community_centre'],
        'icon-image': 'community_centre',
        'icon-color': colorScheme.pointIconCommunityCentreIconColor,
        'text-color': colorScheme.pointIconCommunityCentreTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'fountain'],
        'icon-image': 'fountain',
        'icon-color': colorScheme.pointIconFountainIconColor,
        'text-color': colorScheme.pointIconFountainTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'nightclub'],
        'icon-image': 'nightclub',
        'icon-color': colorScheme.pointIconNigthclubIconColor,
        'text-color': colorScheme.pointIconNightclubTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'public_bookcase'],
        'icon-image': 'public_bookcase',
        'icon-color': colorScheme.pointIconPublicBookcaseIconColor,
        'text-color': colorScheme.pointIconPublicBookCaseTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'theatre'],
        'icon-image': 'theatre',
        'icon-color': colorScheme.pointIconTheatreIconColor,
        'text-color': colorScheme.pointIconTheatreTextColor,
    },

    // Amenity: public service
    {
        filter: ['==', ['get', 'amenity'], 'courthouse'],
        'icon-image': 'courthouse',
        'icon-color': colorScheme.pointIconCourthouseIconColor,
        'text-color': colorScheme.pointIconCourthouseTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'fire_station'],
        'icon-image': 'firestation',
        'icon-color': colorScheme.pointIconFireStationIconColor,
        'text-color': colorScheme.pointIconFireStationTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'police'],
        'icon-image': 'police',
        'icon-color': colorScheme.pointIconPoliceIconColor,
        'text-color': colorScheme.pointIconPoliceTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'post_box'],
        'icon-image': 'post_box',
        'icon-color': colorScheme.pointIconPostBoxIconColor,
        'text-color': colorScheme.pointIconPostBoxTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'post_office'],
        'icon-image': 'post_office',
        'icon-color': colorScheme.pointIconPostOfficeIconColor,
        'text-color': colorScheme.pointIconPostOfficeTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'prison'],
        'icon-image': 'prison',
        'icon-color': colorScheme.pointIconPrisonIconColor,
        'text-color': colorScheme.pointIconPrisonTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'townhall'],
        'icon-image': 'town_hall',
        'icon-color': colorScheme.pointIconTownhallIconColor,
        'text-color': colorScheme.pointIconTowmhallTextColor,
    },

    // Amenity: facilities
    {
        filter: ['==', ['get', 'amenity'], 'bbq'],
        'icon-image': 'bbq',
        'icon-color': colorScheme.pointIconBbqIconColor,
        'text-color': colorScheme.pointIconBbqTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bench'],
        'icon-image': 'bench',
        'icon-color': colorScheme.pointIconBenchIconColor,
        'text-color': colorScheme.pointIconBenchTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'drinking_water'],
        'icon-image': 'drinking_water',
        'icon-color': colorScheme.pointIconDrinkingWaterIconColor,
        'text-color': colorScheme.pointIconDrinkingWaterTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'shelter'],
        'icon-image': 'shelter',
        'icon-color': colorScheme.pointIconShelterIconColor,
        'text-color': colorScheme.pointIconShelterTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'shower'],
        'icon-image': 'shower',
        'icon-color': colorScheme.pointIconShowerIconColor,
        'text-color': colorScheme.pointIconShowerTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'telephone'],
        'icon-image': 'telephone',
        'icon-color': colorScheme.pointIconTelephoneIconColor,
        'text-color': colorScheme.pointIconTelephoneTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'toilets'],
        'icon-image': 'toilets',
        'icon-color': colorScheme.pointIconToiletsIconColor,
        'text-color': colorScheme.pointIconToiletsTextColor,
    },

    // Amenity: waste management
    {
        filter: ['==', ['get', 'amenity'], 'recycling'],
        'icon-image': 'recycling',
        'icon-color': colorScheme.pointIconRecyclingIconColor,
        'text-color': colorScheme.pointIconRecyclingTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'waste_basket'],
        'icon-image': 'waste_basket',
        'icon-color': colorScheme.pointIconWasteBasketIconColor,
        'text-color': colorScheme.pointIconWasteBasketTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'waste_disposal'],
        'icon-image': 'waste_disposal',
        'icon-color': colorScheme.pointIconWasteDisposalIconColor,
        'text-color': colorScheme.pointIconWasteDisposalTextColor,
    },

    // Amenity: Others
    {
        filter: ['==', ['get', 'amenity'], 'childcare'],
        'icon-image': 'place-6',
        'icon-color': colorScheme.pointIconChildcareIconColor,
        'text-color': colorScheme.pointIconChildcareTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'hunting_stand'],
        'icon-image': 'hunting_stand',
        'icon-color': colorScheme.pointIconHuntingStandIconColor,
        'text-color': colorScheme.pointIconHuntingStandTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'internet_cafe'],
        'icon-image': 'internet_cafe',
        'icon-color': colorScheme.pointIconInternetCafeIconColor,
        'text-color': colorScheme.pointIconInternetCafeTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'marketplace'],
        'icon-image': 'marketplace',
        'icon-color': colorScheme.pointIconMarketplaceIconColor,
        'text-color': colorScheme.pointIconMarketplaceTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'place_of_worship'],
        'icon-image': 'place_of_worship',
        'icon-color': colorScheme.pointIconPlaceOfWorkshipIconColor,
        'text-color': colorScheme.pointIconPlaceOfWorkshipTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'public_bath'],
        'icon-image': 'public_bath',
        'icon-color': colorScheme.pointIconPublicBathIconColor,
        'text-color': colorScheme.pointIconPublicBathTextColor,
    },

    // Historic
    {
        filter: ['==', ['get', 'historic'], 'archaeological_site'],
        'icon-image': 'archaeological_site',
        'icon-color': colorScheme.pointIconArchaeologicalSiteIconColor,
        'text-color': colorScheme.pointIconArchaeologicalSiteTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'castle'],
        'icon-image': 'castle',
        'icon-color': colorScheme.pointIconCastleIconColor,
        'text-color': colorScheme.pointIconCastleTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'city_gate'],
        'icon-image': 'city_gate',
        'icon-color': colorScheme.pointIconCityGateIconColor,
        'text-color': colorScheme.pointIconCityGateTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'fort'],
        'icon-image': 'fort',
        'icon-color': colorScheme.pointIconFortIconColor,
        'text-color': colorScheme.pointIconFortTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'manor'],
        'icon-image': 'manor',
        'icon-color': colorScheme.pointIconManorIconColor,
        'text-color': colorScheme.pointIconManorTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'memorial'],
        'icon-image': 'memorial',
        'icon-color': colorScheme.pointIconMemorialIconColor,
        'text-color': colorScheme.pointIconMemorialTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'monument'],
        'icon-image': 'monument',
        'icon-color': colorScheme.pointIconMonumentIconColor,
        'text-color': colorScheme.pointIconMonumentTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'wayside_cross'],
        'icon-image': 'wayside_cross',
        'icon-color': colorScheme.pointIconWaysideCrossIconColor,
        'text-color': colorScheme.pointIconWaysideCrossTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'wayside_shrine'],
        'icon-image': 'wayside_shrine',
        'icon-color': colorScheme.pointIconWayShrineIconColor,
        'text-color': colorScheme.pointIconWaysideShrineTextColor,
    },

    // Leisure
    {
        filter: ['==', ['get', 'leisure'], 'amusement_arcade'],
        'icon-image': 'amusement_arcade',
        'icon-color': colorScheme.pointIconAmusementArcadeIconColor,
        'text-color': colorScheme.pointIconAmusementArcadeTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'beach_resort'],
        'icon-image': 'beach_resort',
        'icon-color': colorScheme.pointIconBeachResortIconColor,
        'text-color': colorScheme.pointIconBeachResortTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'bird_hide'],
        'icon-image': 'bird_hide',
        'icon-color': colorScheme.pointIconBirdHideIconColor,
        'text-color': colorScheme.pointIconBirdHideTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'bowling_alley'],
        'icon-image': 'bowling_alley',
        'icon-color': colorScheme.pointIconBowlingAlleyIconColor,
        'text-color': colorScheme.pointIconBowlingAlleyTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'firepit'],
        'icon-image': 'firepit',
        'icon-color': colorScheme.pointIconFirepitIconColor,
        'text-color': colorScheme.pointIconFirepitTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'fishing'],
        'icon-image': 'fishing',
        'icon-color': colorScheme.pointIconFishingIconColor,
        'text-color': colorScheme.pointIconFishingTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'fitness_centre'],
        'icon-image': 'sports',
        'icon-color': colorScheme.pointIconFitnessCentreIconColor,
        'text-color': colorScheme.pointIconFitnessCentreTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'fitness_station'],
        'icon-image': 'sports',
        'icon-color': colorScheme.pointIconFitnessStationIconColor,
        'text-color': colorScheme.pointIconFitnessStationTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'golf_course'],
        'icon-image': 'golf_course',
        'icon-color': colorScheme.pointIconGolfCourseIconColor,
        'text-color': colorScheme.pointIconGolfCourseTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'miniature_golf'],
        'icon-image': 'miniature_golf',
        'icon-color': colorScheme.pointIconMiniatureGolfIconColor,
        'text-color': colorScheme.pointIconMiniatureGolfTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'outdoor_seating'],
        'icon-image': 'outdoor_seating',
        'icon-color': colorScheme.pointIconOutdoorSeatingIconColor,
        'text-color': colorScheme.pointIconOutdoorSeatingTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'picnic_table'],
        'icon-image': 'picnic',
        'icon-color': colorScheme.pointIconPicnicTableIconColor,
        'text-color': colorScheme.pointIconPicnicTableTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'playground'],
        'icon-image': 'playground',
        'icon-color': colorScheme.pointIconPlaygroundIconColor,
        'text-color': colorScheme.pointIconPlaygroundTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'sauna'],
        'icon-image': 'sauna',
        'icon-color': colorScheme.pointIconSaunaIconColor,
        'text-color': colorScheme.pointIconSaunaTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'slipway'],
        'icon-image': 'slipway',
        'icon-color': colorScheme.pointIconSlipwayIconColor,
        'text-color': colorScheme.pointIconSlipwayTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'swimming_area'],
        'icon-image': 'swimming_area',
        'icon-color': colorScheme.pointIconSwimmingAreaIconColor,
        'text-color': colorScheme.pointIconSwimmingAreaTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'water_park'],
        'icon-image': 'water_park',
        'icon-color': colorScheme.pointIconWaterParkIconColor,
        'text-color': colorScheme.pointIconWaterParkTextColor,
    },

    // Man-made
    {
        filter: ['==', ['get', 'man_made'], 'chimney'],
        'icon-image': 'chimney',
        'icon-color': colorScheme.pointIconChimneyIconColor,
        'text-color': colorScheme.pointIconChimneyTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'communications_tower'],
        'icon-image': 'communications_tower',
        'icon-color': colorScheme.pointIconCommunicationTowerIconColor,
        'text-color': colorScheme.pointIconCommunicationTowerTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'crane'],
        'icon-image': 'crane',
        'icon-color': colorScheme.pointIconCraneIconColor,
        'text-color': colorScheme.pointIconCraneTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'cross'],
        'icon-image': 'cross',
        'icon-color': colorScheme.pointIconCrossIconColor,
        'text-color': colorScheme.pointIconCrossTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'lighthouse'],
        'icon-image': 'lighthouse',
        'icon-color': colorScheme.pointIconLighthouseIconColor,
        'text-color': colorScheme.pointIconLightHouseTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'mast'],
        'icon-image': 'mast',
        'icon-color': colorScheme.pointIconMastIconColor,
        'text-color': colorScheme.pointIconMastTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'obelisk'],
        'icon-image': 'obelisk',
        'icon-color': colorScheme.pointIconObeliskIconColor,
        'text-color': colorScheme.pointIconObeliskTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'silo'],
        'icon-image': 'silo',
        'icon-color': colorScheme.pointIconSiloIconColor,
        'text-color': colorScheme.pointIconSiloTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'storage_tank'],
        'icon-image': 'storage_tank',
        'icon-color': colorScheme.pointIconStorageTankIconColor,
        'text-color': colorScheme.pointIconStorageTankTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'telescope'],
        'icon-image': 'telescope',
        'icon-color': colorScheme.pointIconTelescopeIconColor,
        'text-color': colorScheme.pointIconTelescopeTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'tower'],
        'icon-image': 'tower_generic',
        'icon-color': colorScheme.pointIconTowerIconColor,
        'text-color': colorScheme.pointIconTowerTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'water_tower'],
        'icon-image': 'water_tower',
        'icon-color': colorScheme.pointIconWaterTowerIconColor,
        'text-color': colorScheme.pointIconWaterTowerTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'windmill'],
        'icon-image': 'windmill',
        'icon-color': colorScheme.pointIconWindmillIconColor,
        'text-color': colorScheme.pointIconWindmillTextColor,
    },

    // Military
    {
        filter: ['==', ['get', 'military'], 'bunker'],
        'icon-image': 'bunker',
        'icon-color': colorScheme.pointIconBunkerIconColor,
        'text-color': colorScheme.pointIconBunkerTextColor,
    },

    // Natural
    {
        filter: ['==', ['get', 'natural'], 'spring'],
        'icon-image': 'spring',
        'icon-color': colorScheme.pointIconSpringIconColor,
        'text-color': colorScheme.pointIconSpringTextColor,
    },
    {
        filter: ['==', ['get', 'natural'], 'cave_entrance'],
        'icon-image': 'entrance',
        'icon-color': colorScheme.pointIconCaveEntranceIconColor,
        'text-color': colorScheme.pointIconCaveEntranceTextColor,
    },
    {
        filter: ['==', ['get', 'natural'], 'peak'],
        'icon-image': 'peak',
        'icon-color': colorScheme.pointIconPeakIconColor,
        'text-color': colorScheme.pointIconPeakTextColor,
    },
    {
        filter: ['==', ['get', 'natural'], 'saddle'],
        'icon-image': 'saddle',
        'icon-color': colorScheme.pointIconSaddleIconColor,
        'text-color': colorScheme.pointIconSaddleTextColor,
    },
    {
        filter: ['==', ['get', 'natural'], 'volcano'],
        'icon-image': 'volcano',
        'icon-color': colorScheme.pointIconVolcanoIconColor,
        'text-color': colorScheme.pointIconVolcanoTextColor,
    },

    // Railway: stations and stops
    {
        filter: ['==', ['get', 'railway'], 'halt'],
        'icon-image': 'place-6',
        'icon-color': colorScheme.pointIconHaltIconColor,
        'text-color': colorScheme.pointIconHaltTextColor,
    },
    {
        filter: ['==', ['get', 'railway'], 'station'],
        'icon-image': 'place-6',
        'icon-color': colorScheme.pointIconStationIconColor,
        'text-color': colorScheme.pointIconStationTextColor,
    },
    {
        filter: ['==', ['get', 'railway'], 'subway_entrance'],
        'icon-image': 'entrance',
        'icon-color': colorScheme.pointIconSubwayEntranceIconColor,
        'text-color': colorScheme.pointIconSubwayEntranceTextColor,
    },
    {
        filter: ['==', ['get', 'railway'], 'tram_stop'],
        'icon-image': 'tram_stop',
        'icon-color': colorScheme.pointIconTramStopIconColor,
        'text-color': colorScheme.pointIconTramStopTextColor,
    },

    // Railway: other railways
    {
        filter: ['==', ['get', 'railway'], 'crossing'],
        'icon-image': 'level_crossing',
        'icon-color': colorScheme.pointIconCrossingIconColor,
        'text-color': colorScheme.pointIconCrossingTextColor,
    },
    {
        filter: ['==', ['get', 'railway'], 'level_crossing'],
        'icon-image': 'level_crossing',
        'icon-color': colorScheme.pointIconLevelCrossingIconColor,
        'text-color': colorScheme.pointIconLevelCrossingTextColor,
    },

    // Tourism
    {
        filter: ['==', ['get', 'tourism'], 'alpine_hut'],
        'icon-image': 'alpine_hut',
        'icon-color': colorScheme.pointIconAlpineHutIconColor,
        'text-color': colorScheme.pointIconAlpineHutTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'apartment'],
        'icon-image': 'apartment',
        'icon-color': colorScheme.pointIconApartmentIconColor,
        'text-color': colorScheme.pointIconApartmentTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'artwork'],
        'icon-image': 'artwork',
        'icon-color': colorScheme.pointIconArtworkIconColor,
        'text-color': colorScheme.pointIconArtworkTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'camp_site'],
        'icon-image': 'camping',
        'icon-color': colorScheme.pointIconCampSiteIconColor,
        'text-color': colorScheme.pointIconCampSiteTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'caravan_site'],
        'icon-image': 'caravan_park',
        'icon-color': colorScheme.pointIconCaravaneSiteIconColor,
        'text-color': colorScheme.pointIconCaravaneSiteTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'chalet'],
        'icon-image': 'chalet',
        'icon-color': colorScheme.pointIconChaletIconColor,
        'text-color': colorScheme.pointIconChaletTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'gallery'],
        'icon-image': 'art',
        'icon-color': colorScheme.pointIconGalleryIconColor,
        'text-color': colorScheme.pointIconGalleryTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'guest_house'],
        'icon-image': 'guest_house',
        'icon-color': colorScheme.pointIconGuestHouseIconColor,
        'text-color': colorScheme.pointIconGuestHouseextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'hostel'],
        'icon-image': 'hostel',
        'icon-color': colorScheme.pointIconHostelIconColor,
        'text-color': colorScheme.pointIconHostelTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'motel'],
        'icon-image': 'motel',
        'icon-color': colorScheme.pointIconMotelIconColor,
        'text-color': colorScheme.pointIconMotelTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'museum'],
        'icon-image': 'museum',
        'icon-color': colorScheme.pointIconMuseumIconColor,
        'text-color': colorScheme.pointIconMuseumTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'picnic_site'],
        'icon-image': 'picnic',
        'icon-color': colorScheme.pointIconPicnicSiteIconColor,
        'text-color': colorScheme.pointIconPicnicSiteTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'viewpoint'],
        'icon-image': 'viewpoint',
        'icon-color': colorScheme.pointIconViewpointIconColor,
        'text-color': colorScheme.pointIconViewpointTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'wilderness_hut'],
        'icon-image': 'wilderness_hut',
        'icon-color': colorScheme.pointIconWildernessHutIconColor,
        'text-color': colorScheme.pointIconWildernessHutTextColor,
    },

    // Waterway: barriers on waterways
    {
        filter: ['==', ['get', 'waterway'], 'dam'],
        'icon-image': 'dam',
        'icon-color': colorScheme.pointIconDamIconColor,
        'text-color': colorScheme.pointIconDamTextColor,
    },
    {
        filter: ['==', ['get', 'waterway'], 'weir'],
        'icon-image': 'weir',
        'icon-color': colorScheme.pointIconWeirIconColor,
        'text-color': colorScheme.pointIconWeirTextColor,
    },
    {
        filter: ['==', ['get', 'waterway'], 'waterfall'],
        'icon-image': 'waterfall',
        'icon-color': colorScheme.pointIconWaterfallIconColor,
        'text-color': colorScheme.pointIconWaterfallTextColor,
    },
    {
        filter: ['==', ['get', 'waterway'], 'lock_gate'],
        'icon-image': 'lock_gate',
        'icon-color': colorScheme.pointIconLockGateIconColor,
        'text-color': colorScheme.pointIconLockGateTextColor,
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
        'icon-halo-color': colorScheme.pointIconLayerIconHaloColor,
        'icon-halo-width': 1,
        'text-halo-width': 1,
        'text-halo-color': colorScheme.pointIconLayerTextHaloColor,
    },
});
