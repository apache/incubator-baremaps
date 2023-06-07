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
import colorScheme from "../../colorScheme.js";

let directives = [
    // Amenity: sustenance
    {
        filter: ['==', ['get', 'amenity'], 'bar'],
        'icon-image': 'bar',
        'icon-color': colorScheme.directivesBarIconColor,
        'text-color': colorScheme.directivesBarTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'biergarten'],
        'icon-image': 'biergarten',
        'icon-color': colorScheme.directivesBiergartenIconColor,
        'text-color': colorScheme.directivesBiergartenTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'cafe'],
        'icon-image': 'cafe',
        'icon-color': colorScheme.directivesCafeIconColor,
        'text-color': colorScheme.directivesCafeTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'fast_food'],
        'icon-image': 'fast_food',
        'icon-color': colorScheme.directivesFastFoodIconColor,
        'text-color': colorScheme.directivesFastFoodTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'food_court'],
        'icon-image': 'food_court',
        'icon-color': colorScheme.directivesFoodCourtIconColor,
        'text-color': colorScheme.directivesFoodCourtTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'ice_cream'],
        'icon-image': 'ice_cream',
        'icon-color': colorScheme.directivesIceCreamIconColor,
        'text-color': colorScheme.directivesIceCreamTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'pub'],
        'icon-image': 'pub',
        'icon-color': colorScheme.directivesPubIconColor,
        'text-color': colorScheme.directivesPubTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'restaurant'],
        'icon-image': 'restaurant',
        'icon-color': colorScheme.directivesRestaurantIconColor,
        'text-color': colorScheme.directivesRestaurantTextColor,
    },

    // Amenity: education
    // {
    //     filter: ['==', ['get', 'amenity'], 'driving_school'],
    //     'icon-image': 'driving_school',
    //     'icon-color': colorScheme.directivesAmenityEducationDrivingSchool,
    //     'text-color': colorScheme.directivesAmenityEducationDrivingSchoolTextColor,
    // },
    {
        filter: ['==', ['get', 'amenity'], 'library'],
        'icon-image': 'library',
        'icon-color': colorScheme.directivesLibraryIconColor,
        'text-color': colorScheme.directivesLibraryTextColor,
    },

    // Amenity: transportation
    {
        filter: ['==', ['get', 'amenity'], 'bicycle_parking'],
        'icon-image': 'bicycle_parking',
        'icon-color': colorScheme.directivesBicycleParkingIconColor,
        'text-color': colorScheme.directivesBicycleParkingTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bicycle_repair_station'],
        'icon-image': 'bicycle_repair_station',
        'icon-color': colorScheme.directivesBicycleRepairStationIconColor,
        'text-color': colorScheme.directivesBicycleRepairStationTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bicycle_rental'],
        'icon-image': 'rental_bicycle',
        'icon-color': colorScheme.directivesBicycleRentalIconColor,
        'text-color': colorScheme.directivesBicycleRentalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'boat_rental'],
        'icon-image': 'boat_rental',
        'icon-color': colorScheme.directivesBoatRentalIconColor,
        'text-color': colorScheme.directivesBoatRentalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bus_station'],
        'icon-image': 'bus_station',
        'icon-color': colorScheme.directivesBusStationIconColor,
        'text-color': colorScheme.directivesBusStationTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'car_rental'],
        'icon-image': 'rental_car',
        'icon-color': colorScheme.directivesCarRentalIconColor,
        'text-color': colorScheme.directivesCarRentalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'car_wash'],
        'icon-image': 'car_wash',
        'icon-color': colorScheme.directivesCarWashIconColor,
        'text-color': colorScheme.directivesCarWashTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'vehicle_inspection'],
        'icon-image': 'vehicle_inspection',
        'icon-color': colorScheme.directivesVehicleInspectionIconColor,
        'text-color': colorScheme.directivesVehicleInspectionTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'charging_station'],
        'icon-image': 'charging_station',
        'icon-color': colorScheme.directivesChargingStationIconColor,
        'text-color': colorScheme.directivesChargingStationTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'ferry_terminal'],
        'icon-image': 'ferry',
        'icon-color': colorScheme.directivesFerryTerminalIconColor,
        'text-color': colorScheme.directivesFerryTerminalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'fuel'],
        'icon-image': 'fuel',
        'icon-color': colorScheme.directivesFuelIconColor,
        'text-color': colorScheme.directivesFuelTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'motorcycle_parking'],
        'icon-image': 'motorcycle_parking',
        'icon-color': colorScheme.directivesMotorcycleParkingIconColor,
        'text-color': colorScheme.directivesMotorcycleParkingTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'parking'],
        'icon-image': 'parking',
        'icon-color': colorScheme.directivesParkingIconColor,
        'text-color': colorScheme.directivesParkingTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'parking_entrance'],
        'icon-image': 'entrance',
        'icon-color': colorScheme.directivesParkingEntranceIconColor,
        'text-color': colorScheme.directivesParkingEntranceTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'taxi'],
        'icon-image': 'taxi',
        'icon-color': colorScheme.directivesTaxiIconColor,
        'text-color': colorScheme.directivesTaxiTextColor,
    },
    {
        filter: ['==', ['get', 'highway'], 'bus_stop'],
        'icon-image': 'bus_stop',
        'icon-color': colorScheme.directivesBusStopIconColor,
        'text-color': colorScheme.directivesBusStopTextColo,
    },

    // Amenity: financial
    {
        filter: ['==', ['get', 'amenity'], 'atm'],
        'icon-image': 'atm',
        'icon-color': colorScheme.directivesAtmIconColor,
        'text-color': colorScheme.directivesAtmTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bank'],
        'icon-image': 'bank',
        'icon-color': colorScheme.directivesBankIconColor,
        'text-color': colorScheme.directivesBankTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bureau_de_change'],
        'icon-image': 'bureau_de_change',
        'icon-color': colorScheme.directivesBureauDeChangeIconColor,
        'text-color': colorScheme.directivesBureauDeChangeTextColor,
    },

    // Amenity: healthcare
    {
        filter: ['==', ['get', 'amenity'], 'clinic'],
        'icon-image': 'hospital',
        'icon-color': colorScheme.directivesCliniqueIconColor,
        'text-color': colorScheme.directivesCliniqueTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'dentist'],
        'icon-image': 'dentist',
        'icon-color': colorScheme.directivesDentistIconColor,
        'text-color': colorScheme.directivesDentistTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'doctors'],
        'icon-image': 'doctors',
        'icon-color': colorScheme.directivesDoctorIconColor,
        'text-color': colorScheme.directivesDoctorTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'hospital'],
        'icon-image': 'hospital',
        'icon-color': colorScheme.directivesHospitalIconColor,
        'text-color': colorScheme.directivesHospitalTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'nursing_home'],
        'icon-image': 'nursing_home',
        'icon-color': colorScheme.directivesNursingHomeIconColor,
        'text-color': colorScheme.directivesNursingHomeTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'pharmacy'],
        'icon-image': 'pharmacy',
        'icon-color': colorScheme.directivesPharmacieIconColor,
        'text-color': colorScheme.directivesPharmacieTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'social_facility'],
        'icon-image': 'social_facility',
        'icon-color': colorScheme.directivesSocialFacilityIconColor,
        'text-color': colorScheme.directivesSocialFacilityTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'veterinary'],
        'icon-image': 'veterinary',
        'icon-color': colorScheme.directivesVeterinaryIconColor,
        'text-color': colorScheme.directivesVeterinaryTextColor,
    },

    // Amenity: entertainment, arts & culture
    {
        filter: ['==', ['get', 'amenity'], 'arts_centre'],
        'icon-image': 'arts_centre',
        'icon-color': colorScheme.directivesArtsCentreIconColor,
        'text-color': colorScheme.directivesArtsCentreTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'casino'],
        'icon-image': 'casino',
        'icon-color': colorScheme.directivesCasinoIconColor,
        'text-color': colorScheme.directivesCasinoTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'cinema'],
        'icon-image': 'cinema',
        'icon-color': colorScheme.directivesCinemaIconColor,
        'text-color': colorScheme.directivesCinemaTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'community_centre'],
        'icon-image': 'community_centre',
        'icon-color': colorScheme.directivesCommunityCentreIconColor,
        'text-color': colorScheme.directivesCommunityCentreTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'fountain'],
        'icon-image': 'fountain',
        'icon-color': colorScheme.directivesFountainIconColor,
        'text-color': colorScheme.directivesFountainTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'nightclub'],
        'icon-image': 'nightclub',
        'icon-color': colorScheme.directivesNigthclubIconColor,
        'text-color': colorScheme.directivesNightclubTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'public_bookcase'],
        'icon-image': 'public_bookcase',
        'icon-color': colorScheme.directivesPublicBookcaseIconColor,
        'text-color': colorScheme.directivesPublicBookCaseTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'theatre'],
        'icon-image': 'theatre',
        'icon-color': colorScheme.directivesTheatreIconColor,
        'text-color': colorScheme.directivesTheatreTextColor,
    },

    // Amenity: public service
    {
        filter: ['==', ['get', 'amenity'], 'courthouse'],
        'icon-image': 'courthouse',
        'icon-color': colorScheme.directivesCourthouseIconColor,
        'text-color': colorScheme.directivesCourthouseTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'fire_station'],
        'icon-image': 'firestation',
        'icon-color': colorScheme.directivesFireStationIconColor,
        'text-color': colorScheme.directivesFireStationTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'police'],
        'icon-image': 'police',
        'icon-color': colorScheme.directivesPoliceIconColor,
        'text-color': colorScheme.directivesPoliceTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'post_box'],
        'icon-image': 'post_box',
        'icon-color': colorScheme.directivesPostBoxIconColor,
        'text-color': colorScheme.directivesPostBoxTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'post_office'],
        'icon-image': 'post_office',
        'icon-color': colorScheme.directivesPostOfficeIconColor,
        'text-color': colorScheme.directivesPostOfficeTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'prison'],
        'icon-image': 'prison',
        'icon-color': colorScheme.directivesPrisonIconColor,
        'text-color': colorScheme.directivesPrisonTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'townhall'],
        'icon-image': 'town_hall',
        'icon-color': colorScheme.directivesTownhallIconColor,
        'text-color': colorScheme.directivesTowmhallTextColor,
    },

    // Amenity: facilities
    {
        filter: ['==', ['get', 'amenity'], 'bbq'],
        'icon-image': 'bbq',
        'icon-color': colorScheme.directivesBbqIconColor,
        'text-color': colorScheme.directivesBbqTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'bench'],
        'icon-image': 'bench',
        'icon-color': colorScheme.directivesBenchIconColor,
        'text-color': colorScheme.directivesBenchTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'drinking_water'],
        'icon-image': 'drinking_water',
        'icon-color': colorScheme.directivesDrinkingWaterIconColor,
        'text-color': colorScheme.directivesDrinkingWaterTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'shelter'],
        'icon-image': 'shelter',
        'icon-color': colorScheme.directivesShelterIconColor,
        'text-color': colorScheme.directivesShelterTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'shower'],
        'icon-image': 'shower',
        'icon-color': colorScheme.directivesShowerIconColor,
        'text-color': colorScheme.directivesShowerTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'telephone'],
        'icon-image': 'telephone',
        'icon-color': colorScheme.directivesTelephoneIconColor,
        'text-color': colorScheme.directivesTelephoneTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'toilets'],
        'icon-image': 'toilets',
        'icon-color': colorScheme.directivesToiletsIconColor,
        'text-color': colorScheme.directivesToiletsTextColor,
    },

    // Amenity: waste management
    {
        filter: ['==', ['get', 'amenity'], 'recycling'],
        'icon-image': 'recycling',
        'icon-color': colorScheme.directivesRecyclingIconColor,
        'text-color': colorScheme.directivesRecyclingTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'waste_basket'],
        'icon-image': 'waste_basket',
        'icon-color': colorScheme.directivesWasteBasketIconColor,
        'text-color': colorScheme.directivesWasteBasketTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'waste_disposal'],
        'icon-image': 'waste_disposal',
        'icon-color': colorScheme.directivesWasteDisposalIconColor,
        'text-color': colorScheme.directivesWasteDisposalTextColor,
    },

    // Amenity: Others
    {
        filter: ['==', ['get', 'amenity'], 'childcare'],
        'icon-image': 'place-6',
        'icon-color': colorScheme.directivesChildcareIconColor,
        'text-color': colorScheme.directivesChildcareTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'hunting_stand'],
        'icon-image': 'hunting_stand',
        'icon-color': colorScheme.directivesHuntingStandIconColor,
        'text-color': colorScheme.directivesHuntingStandTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'internet_cafe'],
        'icon-image': 'internet_cafe',
        'icon-color': colorScheme.directivesInternetCafeIconColor,
        'text-color': colorScheme.directivesInternetCafeTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'marketplace'],
        'icon-image': 'marketplace',
        'icon-color': colorScheme.directivesMarketplaceIconColor,
        'text-color': colorScheme.directivesMarketplaceTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'place_of_worship'],
        'icon-image': 'place_of_worship',
        'icon-color': colorScheme.directivesPlaceOfWorkshipIconColor,
        'text-color': colorScheme.directivesPlaceOfWorkshipTextColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'public_bath'],
        'icon-image': 'public_bath',
        'icon-color': colorScheme.directivesPublicBathIconColor,
        'text-color': colorScheme.directivesPublicBathTextColor,
    },

    // Historic
    {
        filter: ['==', ['get', 'historic'], 'archaeological_site'],
        'icon-image': 'archaeological_site',
        'icon-color': colorScheme.directivesArchaeologicalSiteIconColor,
        'text-color': colorScheme.directivesArchaeologicalSiteTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'castle'],
        'icon-image': 'castle',
        'icon-color': colorScheme.directivesCastleIconColor,
        'text-color': colorScheme.directivesCastleTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'city_gate'],
        'icon-image': 'city_gate',
        'icon-color': colorScheme.directivesCityGateIconColor,
        'text-color': colorScheme.directivesCityGateTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'fort'],
        'icon-image': 'fort',
        'icon-color': colorScheme.directivesFortIconColor,
        'text-color': colorScheme.directivesFortTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'manor'],
        'icon-image': 'manor',
        'icon-color': colorScheme.directivesManorIconColor,
        'text-color': colorScheme.directivesManorTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'memorial'],
        'icon-image': 'memorial',
        'icon-color': colorScheme.directivesMemorialIconColor,
        'text-color': colorScheme.directivesMemorialTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'monument'],
        'icon-image': 'monument',
        'icon-color': colorScheme.directivesMonumentIconColor,
        'text-color': colorScheme.directivesMonumentTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'wayside_cross'],
        'icon-image': 'wayside_cross',
        'icon-color': colorScheme.directivesWaysideCrossIconColor,
        'text-color': colorScheme.directivesWaysideCrossTextColor,
    },
    {
        filter: ['==', ['get', 'historic'], 'wayside_shrine'],
        'icon-image': 'wayside_shrine',
        'icon-color': colorScheme.directivesWayShrineIconColor,
        'text-color': colorScheme.directivesWaysideShrineTextColor,
    },

    // Leisure
    {
        filter: ['==', ['get', 'leisure'], 'amusement_arcade'],
        'icon-image': 'amusement_arcade',
        'icon-color': colorScheme.directivesAmusementArcadeIconColor,
        'text-color': colorScheme.directivesAmusementArcadeTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'beach_resort'],
        'icon-image': 'beach_resort',
        'icon-color': colorScheme.directivesBeachResortIconColor,
        'text-color': colorScheme.directivesBeachResortTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'bird_hide'],
        'icon-image': 'bird_hide',
        'icon-color': colorScheme.directivesBirdHideIconColor,
        'text-color': colorScheme.directivesBirdHideTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'bowling_alley'],
        'icon-image': 'bowling_alley',
        'icon-color': colorScheme.directivesBowlingAlleyIconColor,
        'text-color': colorScheme.directivesBowlingAlleyTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'firepit'],
        'icon-image': 'firepit',
        'icon-color': colorScheme.directivesFirepitIconColor,
        'text-color': colorScheme.directivesFirepitTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'fishing'],
        'icon-image': 'fishing',
        'icon-color': colorScheme.directivesFishingIconColor,
        'text-color': colorScheme.directivesFishingTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'fitness_centre'],
        'icon-image': 'sports',
        'icon-color': colorScheme.directivesFitnessCentreIconColor,
        'text-color': colorScheme.directivesFitnessCentreTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'fitness_station'],
        'icon-image': 'sports',
        'icon-color': colorScheme.directivesFitnessStationIconColor,
        'text-color': colorScheme.directivesFitnessStationTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'golf_course'],
        'icon-image': 'golf_course',
        'icon-color': colorScheme.directivesGolfCourseIconColor,
        'text-color': colorScheme.directivesGolfCourseTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'miniature_golf'],
        'icon-image': 'miniature_golf',
        'icon-color': colorScheme.directivesMiniatureGolfIconColor,
        'text-color': colorScheme.directivesMiniatureGolfTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'outdoor_seating'],
        'icon-image': 'outdoor_seating',
        'icon-color': colorScheme.directivesOutdoorSeatingIconColor,
        'text-color': colorScheme.directivesOutdoorSeatingTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'picnic_table'],
        'icon-image': 'picnic',
        'icon-color': colorScheme.directivesPicnicTableIconColor,
        'text-color': colorScheme.directivesPicnicTableTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'playground'],
        'icon-image': 'playground',
        'icon-color': colorScheme.directivesPlaygroundIconColor,
        'text-color': colorScheme.directivesPlaygroundTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'sauna'],
        'icon-image': 'sauna',
        'icon-color': colorScheme.directivesSaunaIconColor,
        'text-color': colorScheme.directivesSaunaTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'slipway'],
        'icon-image': 'slipway',
        'icon-color': colorScheme.directivesSlipwayIconColor,
        'text-color': colorScheme.directivesSlipwayTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'swimming_area'],
        'icon-image': 'swimming_area',
        'icon-color': colorScheme.directivesSwimmingAreaIconColor,
        'text-color': colorScheme.directivesSwimmingAreaTextColor,
    },
    {
        filter: ['==', ['get', 'leisure'], 'water_park'],
        'icon-image': 'water_park',
        'icon-color': colorScheme.directivesWaterParkIconColor,
        'text-color': colorScheme.directivesWaterParkTextColor,
    },

    // Man-made
    {
        filter: ['==', ['get', 'man_made'], 'chimney'],
        'icon-image': 'chimney',
        'icon-color': colorScheme.directivesChimneyIconColor,
        'text-color': colorScheme.directivesChimneyTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'communications_tower'],
        'icon-image': 'communications_tower',
        'icon-color': colorScheme.directivesCommunicationTowerIconColor,
        'text-color': colorScheme.directivesCommunicationTowerTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'crane'],
        'icon-image': 'crane',
        'icon-color': colorScheme.directivesCraneIconColor,
        'text-color': colorScheme.directivesCraneTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'cross'],
        'icon-image': 'cross',
        'icon-color': colorScheme.directivesCrossIconColor,
        'text-color': colorScheme.directivesCrossTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'lighthouse'],
        'icon-image': 'lighthouse',
        'icon-color': colorScheme.directivesLighthouseIconColor,
        'text-color': colorScheme.directivesLightHouseTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'mast'],
        'icon-image': 'mast',
        'icon-color': colorScheme.directivesMastIconColor,
        'text-color': colorScheme.directivesMastTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'obelisk'],
        'icon-image': 'obelisk',
        'icon-color': colorScheme.directivesObeliskIconColor,
        'text-color': colorScheme.directivesObeliskTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'silo'],
        'icon-image': 'silo',
        'icon-color': colorScheme.directivesSiloIconColor,
        'text-color': colorScheme.directivesSiloTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'storage_tank'],
        'icon-image': 'storage_tank',
        'icon-color': colorScheme.directivesStorageTankIconColor,
        'text-color': colorScheme.directivesStorageTankTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'telescope'],
        'icon-image': 'telescope',
        'icon-color': colorScheme.directivesTelescopeIconColor,
        'text-color': colorScheme.directivesTelescopeTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'tower'],
        'icon-image': 'tower_generic',
        'icon-color': colorScheme.directivesTowerIconColor,
        'text-color': colorScheme.directivesTowerTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'water_tower'],
        'icon-image': 'water_tower',
        'icon-color': colorScheme.directivesWaterTowerIconColor,
        'text-color': colorScheme.directivesWaterTowerTextColor,
    },
    {
        filter: ['==', ['get', 'man_made'], 'windmill'],
        'icon-image': 'windmill',
        'icon-color': colorScheme.directivesWindmillIconColor,
        'text-color': colorScheme.directivesWindmillTextColor,
    },

    // Military
    {
        filter: ['==', ['get', 'military'], 'bunker'],
        'icon-image': 'bunker',
        'icon-color': colorScheme.directivesBunkerIconColor,
        'text-color': colorScheme.directivesBunkerTextColor,
    },

    // Natural
    {
        filter: ['==', ['get', 'natural'], 'spring'],
        'icon-image': 'spring',
        'icon-color': colorScheme.directivesSpringIconColor,
        'text-color': colorScheme.directivesSpringTextColor,
    },
    {
        filter: ['==', ['get', 'natural'], 'cave_entrance'],
        'icon-image': 'entrance',
        'icon-color': colorScheme.directivesCaveEntranceIconColor,
        'text-color': colorScheme.directivesCaveEntranceTextColor,
    },
    {
        filter: ['==', ['get', 'natural'], 'peak'],
        'icon-image': 'peak',
        'icon-color': colorScheme.directivesPeakIconColor,
        'text-color': colorScheme.directivesPeakTextColor,
    },
    {
        filter: ['==', ['get', 'natural'], 'saddle'],
        'icon-image': 'saddle',
        'icon-color': colorScheme.directivesSaddleIconColor,
        'text-color': colorScheme.directivesSaddleTextColor,
    },
    {
        filter: ['==', ['get', 'natural'], 'volcano'],
        'icon-image': 'volcano',
        'icon-color': colorScheme.directivesVolcanoIconColor,
        'text-color': colorScheme.directivesVolcanoTextColor,
    },

    // Railway: stations and stops
    {
        filter: ['==', ['get', 'railway'], 'halt'],
        'icon-image': 'place-6',
        'icon-color': colorScheme.directivesHaltIconColor,
        'text-color': colorScheme.directivesHaltTextColor,
    },
    {
        filter: ['==', ['get', 'railway'], 'station'],
        'icon-image': 'place-6',
        'icon-color': colorScheme.directivesStationIconColor,
        'text-color': colorScheme.directivesStationTextColor,
    },
    {
        filter: ['==', ['get', 'railway'], 'subway_entrance'],
        'icon-image': 'entrance',
        'icon-color': colorScheme.directivesSubwayEntranceIconColor,
        'text-color': colorScheme.directivesSubwayEntranceTextColor,
    },
    {
        filter: ['==', ['get', 'railway'], 'tram_stop'],
        'icon-image': 'tram_stop',
        'icon-color': colorScheme.directivesTramStopIconColor,
        'text-color': colorScheme.directivesTramStopTextColor,
    },

    // Railway: other railways
    {
        filter: ['==', ['get', 'railway'], 'crossing'],
        'icon-image': 'level_crossing',
        'icon-color': colorScheme.directivesCrossingIconColor,
        'text-color': colorScheme.directivesCrossingTextColor,
    },
    {
        filter: ['==', ['get', 'railway'], 'level_crossing'],
        'icon-image': 'level_crossing',
        'icon-color': colorScheme.directivesLevelCrossingIconColor,
        'text-color': colorScheme.directivesLevelCrossingTextColor,
    },

    // Tourism
    {
        filter: ['==', ['get', 'tourism'], 'alpine_hut'],
        'icon-image': 'alpine_hut',
        'icon-color': colorScheme.directivesAlpineHutIconColor,
        'text-color': colorScheme.directivesAlpineHutTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'apartment'],
        'icon-image': 'apartment',
        'icon-color': colorScheme.directivesApartmentIconColor,
        'text-color': colorScheme.directivesApartmentTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'artwork'],
        'icon-image': 'artwork',
        'icon-color': colorScheme.directivesArtworkIconColor,
        'text-color': colorScheme.directivesArtworkTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'camp_site'],
        'icon-image': 'camping',
        'icon-color': colorScheme.directivesCampSiteIconColor,
        'text-color': colorScheme.directivesCampSiteTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'caravan_site'],
        'icon-image': 'caravan_park',
        'icon-color': colorScheme.directivesCaravaneSiteIconColor,
        'text-color': colorScheme.directivesCaravaneSiteTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'chalet'],
        'icon-image': 'chalet',
        'icon-color': colorScheme.directivesChaletIconColor,
        'text-color': colorScheme.directivesChaletTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'gallery'],
        'icon-image': 'art',
        'icon-color': colorScheme.directivesGalleryIconColor,
        'text-color': colorScheme.directivesGalleryTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'guest_house'],
        'icon-image': 'guest_house',
        'icon-color': colorScheme.directivesGuestHouseIconColor,
        'text-color': colorScheme.directivesGuestHouseextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'hostel'],
        'icon-image': 'hostel',
        'icon-color': colorScheme.directivesHostelIconColor,
        'text-color': colorScheme.directivesHostelTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'motel'],
        'icon-image': 'motel',
        'icon-color': colorScheme.directivesMotelIconColor,
        'text-color': colorScheme.directivesMotelTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'museum'],
        'icon-image': 'museum',
        'icon-color': colorScheme.directivesMuseumIconColor,
        'text-color': colorScheme.directivesMuseumTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'picnic_site'],
        'icon-image': 'picnic',
        'icon-color': colorScheme.directivesPicnicSiteIconColor,
        'text-color': colorScheme.directivesPicnicSiteTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'viewpoint'],
        'icon-image': 'viewpoint',
        'icon-color': colorScheme.directivesViewpointIconColor,
        'text-color': colorScheme.directivesViewpointTextColor,
    },
    {
        filter: ['==', ['get', 'tourism'], 'wilderness_hut'],
        'icon-image': 'wilderness_hut',
        'icon-color': colorScheme.directivesWildernessHutIconColor,
        'text-color': colorScheme.directivesWildernessHutTextColor,
    },

    // Waterway: barriers on waterways
    {
        filter: ['==', ['get', 'waterway'], 'dam'],
        'icon-image': 'dam',
        'icon-color': colorScheme.directivesDamIconColor,
        'text-color': colorScheme.directivesDamTextColor,
    },
    {
        filter: ['==', ['get', 'waterway'], 'weir'],
        'icon-image': 'weir',
        'icon-color': colorScheme.directivesWeirdIconColor,
        'text-color': colorScheme.directivesWeirdTextColor,
    },
    {
        filter: ['==', ['get', 'waterway'], 'waterfall'],
        'icon-image': 'waterfall',
        'icon-color': colorScheme.directivesWaterfallIconColor,
        'text-color': colorScheme.directivesWaterfallTextColor,
    },
    {
        filter: ['==', ['get', 'waterway'], 'lock_gate'],
        'icon-image': 'lock_gate',
        'icon-color': colorScheme.directivesLockGateIconColor,
        'text-color': colorScheme.directivesLockGateTextColor,
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
        'icon-halo-color': colorScheme.iconsIconHaloColor,
        'icon-halo-width': 1,
        'text-halo-width': 1,
        'text-halo-color': colorScheme.iconsTextHaloColor,
    },
});
