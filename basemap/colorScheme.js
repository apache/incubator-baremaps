
//thene ä disposition: 'positron'

const theme="positron";



function colorScheme(string){
    //trouver comment choisir quelle methode on souhaite appliquer
    switch(theme){
        case 'positron':
            return positronScheme(string);
            break;
        default: return string;
    }
};
//themes de couleur
function positronScheme(string){

    const table=cutRgbString(string)
    console.log(table);
    let newColor= positronColorCalculator(table);
    return giveNewColorString(table, newColor, newColor, newColor);
};


function positronColorCalculator(table){
    return Math.round((parseInt(table[0])+parseInt(table[1])+parseInt(table[2]))/3);
};

//Fonctionnalites
function giveNewColorString(table, newRed, newGreen, newBlue){
    if(table[3]!=="1"){
        let opacity=table[3];
        return `rgba(${newRed},${newGreen},${newBlue},${opacity})`;
    }
    else{
        return `rgb(${newRed},${newGreen},${newBlue})`
    }
};

function cutRgbString(string){
    const firstSeparationCharacter="(";
    const middleSeparationCharacter=",";
    const lastSeparationCharacter=")";

    let greenSubstring= string.substring(string.indexOf(middleSeparationCharacter)+1, string.length);

    let redElement=string.substring(string.indexOf(firstSeparationCharacter)+1,string.indexOf(middleSeparationCharacter));
    let greenElement = greenSubstring.substring(0,greenSubstring.indexOf(middleSeparationCharacter));
    let blueElement;
    let opacity;

    if(hasOpacity(string)){
        let blueSubstring=greenSubstring.substring(greenSubstring.indexOf(middleSeparationCharacter)+1, greenSubstring.length);
        blueElement=blueSubstring.substring(0, blueSubstring.indexOf(middleSeparationCharacter));
        opacity=blueSubstring.substring(blueSubstring.indexOf(middleSeparationCharacter+1), blueSubstring.length)}

    else{
        blueElement=greenSubstring.substring(greenSubstring.indexOf(middleSeparationCharacter)+1, greenSubstring.indexOf(lastSeparationCharacter));
        opacity="1"}

    return [redElement,greenElement,blueElement, opacity];
};
function hasOpacity(string){
    return(string.substring(0,4)==="rgba(");
 };




// This file describes a theme for a map. Its style follows the OpenStreetMap-Carto conventions. Can you generate a new style that respect the exact same sementic but with a positron style.

export default {
    //Highway
    //HighwayLine
    highwayLineMotorwayLineColor: colorScheme('rgb(233, 144, 161)'),
    highwayLineTrunkLineColor: colorScheme('rgb(250, 193, 172)'),
    highwayLinePrimaryLineColor: colorScheme('rgb(253, 221, 179)'),
    highwayLineSecondaryLineColor: colorScheme('rgb(248, 250, 202)'),
    highwayLineTertiaryLineColor: colorScheme('rgb(254, 254, 254)'),
    highwayLineUnclassifiedLineColor: colorScheme('rgb(254, 254, 254)'),
    highwayLineResidentialLineColor: colorScheme('rgb(254, 254, 254)'),
    highwayLineLivingStreetLineColor: colorScheme('rgb(237, 237, 237)'),
    highwayLineServiceLineColor: colorScheme('rgb(254, 254, 254)'),
    highwayLineRacewayLineColor: colorScheme('rgb(255, 192, 203)'),
    highwayLinePedestrianLineColor: colorScheme('rgb(221, 221, 231)'),
    highwayLineBuswayLineColor: colorScheme('rgb(254, 254, 254)'),
    //Construction_dash
    directivesConstructionMotorwayLineColor: colorScheme('rgb(254, 254, 254)'),
    directivesConstructionTrunkLineColor: colorScheme('rgb(254, 254, 254)'),
    directivesConstructionPrimaryLineColor: colorScheme('rgb(254, 254, 254)'),
    directivesConstructionSecondaryLineColor: colorScheme('rgb(254, 254, 254)'),
    directivesConstructionTertiaryLineColor: colorScheme('rgb(254, 254, 254)'),
    directivesConstructionUnclassifiedLineColor: colorScheme('rgb(254, 254, 254)'),
    directivesConstructionResidentialLineColor: colorScheme('rgb(254, 254, 254)'),
    directivesConstructionLivingStreetLineColor: colorScheme('rgb(254, 254, 254)'),
    directivesConstructionServiceLineColor: colorScheme('rgb(254, 254, 254)'),
    //Construction_line
    directivesConstructionMotorwaLinkLineColor: colorScheme('rgb(233, 144, 161)'),
    directivesConstructionTrunkLinkLineColor: colorScheme('rgb(250, 193, 172)'),
    directivesConstructionPrimaryLinkLineColor: colorScheme('rgb(253, 221, 179)'),
    directivesConstructionSecondaryLinkLineColor: colorScheme('rgb(248, 250, 202)'),
    directivesConstructionTertiaryLinkLineColor: colorScheme('rgb(190, 189, 188)'),
    directivesConstructionAllUnclassifiedLineColor: colorScheme('rgb(211, 207, 206)'),
    directivesConstructionAllResidentialLineColor: colorScheme('rgb(211, 207, 206)'),
    directivesConstructionAllLivingStreetLineColor: colorScheme('rgb(207, 207, 207)'),
    directivesConstructionAllServiceLineColor: colorScheme('rgb(213, 211, 211)'),
    directivesConstructionRacewayLineColor: colorScheme('rgb(213, 211, 211)'),
    
    //BridgeLine
    bridgeLineMotorwayLineColor: colorScheme('rgb(227, 113, 134)'),
    bridgeLineTrunkLineColor: colorScheme('rgb(248, 163, 132)'),
    bridgeLinePrimaryLineColor: colorScheme('rgb(252, 202, 137)'),
    bridgeLineSecondaryLineColor: colorScheme('rgb(243, 246, 161)'),
    bridgeLineTertiaryLineColor: colorScheme('rgb(229, 229, 229)'),
    bridgeLineUnclassifiedLineColor: colorScheme('rgb(229, 229, 229)'),
    bridgeLineResidentialLineColor: colorScheme('rgb(229, 229, 229)'),
    bridgeLineLivingStreetLineColor: colorScheme('rgb(213, 213, 213)'),
    bridgeLineServiceLineColor: colorScheme('rgb(229, 229, 229)'),
    bridgeLineTrackLineColor: colorScheme('rgb(159, 126, 57)'),
    bridgeLineRacewayLineColor: colorScheme('rgb(255, 147, 166)'),
    bridgeLinePedestrianLineColor: colorScheme('rgb(194, 194, 212)'),
    //BridgeOUtline
    bridgeOutlineMotorwayLineColor: colorScheme('rgb(223, 55, 106)'),
    bridgeOutlineTrunkLineColor: colorScheme('rgb(212, 91, 54)'),
    bridgeOutlinePrimaryLineColor: colorScheme('rgb(173, 132, 56)'),
    bridgeOutlineSecondaryLineColor: colorScheme('rgb(139, 149, 60)'),
    bridgeOutlineTertiaryLineColor: colorScheme('rgb(171, 170, 169)'),
    bridgeOutlineUnclassifiedLineColor: colorScheme('rgb(191, 185, 184)'),
    bridgeOutlineResidentialLineColor: colorScheme('rgb(191, 185, 184)'),
    bridgeOutlineLivingStreetLineColor: colorScheme('rgb(186, 186, 186)'),
    bridgeOutlineServiceLineColor: colorScheme('rgb(192, 189, 189)'),
    bridgeOutlinePedestrianLineColor: colorScheme('rgb(166, 165, 163)'),
    //HighwayDash
    highwayDashBridlewayLineColor: colorScheme('rgb(68, 159, 66)'),
    highwayDashBuswayLineColor: colorScheme('rgb(0, 146, 219)'),
    highwayDashCyclewayLineColor: colorScheme('rgba(28, 27, 254, 1)'),
    highwayDashFootwayLineColor: colorScheme('rgb(192, 192, 192)'),
    highwayDashHighwayLineColor: colorScheme('rgb(250, 132, 117)'),
    highwayDashTrackLineColor: colorScheme('rgb(177, 140, 63)'),
    //HighwayOutline
    highwayOutlineMotorwayLineColor: colorScheme('rgb(227, 82, 126)'),
    highwayOutlineTrunkLineColor: colorScheme('rgb(217, 111, 78)'),
    highwayOutlinePrimaryLineColor: colorScheme('rgb(192, 147, 62)'),
    highwayOutlineSecondaryLineColor: colorScheme('rgb(154, 166, 67)'),
    highwayOutlineTertiaryLineColor: colorScheme('rgb(190, 189, 188)'),
    highwayOutlineBuswayLineColor: colorScheme('rgb(190, 189, 188)'),
    highwayOutlineUnclassifiedLineColor: colorScheme('rgb(211, 207, 206)'),
    highwayOutlineResidentialLineColor: colorScheme('rgb(211, 207, 206)'),
    highwayOutlineLivingStreetLineColor: colorScheme('rgb(207, 207, 207)'),
    highwayOutlineServiceLineColor: colorScheme('rgb(213, 211, 211)'),
    highwayOutlinePedestrianLineColor: colorScheme('rgb(184, 183, 182)'),
    highwayOutlinePedestrianBisLineColor: colorScheme('rgb(184, 183, 182)'),
    //TunnelLine
    tunnelLineMotorwayLineColor: colorScheme('rgba(241, 188, 198, 1)'),
    tunnelLineTrunkLineColor: colorScheme('rgba(252, 215, 204, 1)'),
    tunnelLinePrimaryLineColor: colorScheme('rgba(254, 237, 213, 1)'),
    tunnelLineSecondaryLineColor: colorScheme('rgba(249, 253, 215, 1)'),
    tunnelLineTertiaryLineColor: colorScheme('rgba(255, 255, 255, 1)'),
    tunnelLineUnclassifiedLineColor: colorScheme('rgba(242, 242, 242, 1)'),
    tunnelLineResidentialLineColor: colorScheme('rgba(211, 207, 206, 1)'),
    tunnelLineLivingStreetLineColor: colorScheme('rgba(245, 245, 245, 1)'),
    tunnelLineServiceLineColor: colorScheme('rgba(242, 242, 242, 1)'),
    tunnelLinePedestrianLineColor: colorScheme('rgba(221, 221, 232, 1)'),
    tunnelLineRacewayLineColor: colorScheme('rgba(255, 192, 203, 1)'),
    tunnelLineTrackLineColor: colorScheme('rgb(177, 140, 63)'),
    //TunnelOutline
    tunnelOutlineMotorwayLineColor: colorScheme('rgba(227, 82, 126, 1)'),
    tunnelOutlineTrunkLineColor: colorScheme('rgba(217, 111, 78, 1)'),
    tunnelOutlinePrimaryLineColor: colorScheme('rgba(192, 147, 62, 1)'),
    tunnelOutlineSecondaryLineColor: colorScheme('rgba(154, 166, 67, 1)'),
    tunnelOutlineTertiaryLineColor: colorScheme('rgba(190, 189, 188, 1)'),
    tunnelOutlineUnclassifiedLineColor: colorScheme('rgba(211, 207, 206, 1)'),
    tunnelOutlineResidentialLineColor: colorScheme('rgba(211, 207, 206, 1)'),
    tunnelOutlineLivingStreetLineColor: colorScheme('rgba(207, 207, 207, 1)'),
    tunnelOutlineServiceLineColor: colorScheme('rgba(213, 211, 211, 1)'),
    tunnelOutlinePedestrianLineColor: colorScheme('rgba(184, 183, 182, 1)'),
    //HighwayLabel
    highwayLabelPaintTextColor: colorScheme('rgb(96,96,96)'),
    highwayLabelPaintTextHaloColor: colorScheme('rgba(255,255,255,0.8)'),
    //PedestrianArea
    pedestrianAreaPaintFillColor: colorScheme('rgb(221, 221, 231)'),

    //Aerialway
    //style
    aerialwayLinePaintLineColor: colorScheme('rgb(177, 177, 175)'),

    //Aeroway
    //Line &&Polygon
    directiveLineColor: colorScheme('rgba(187, 187, 204, 1.0)'),

    //Amenity
    //Background
    directivesKinderGartenFillColor: colorScheme('rgb(255, 255, 228)'),
    directivesSchoolFillColor: colorScheme('rgb(255, 255, 228)'),
    directivesCollegeFillColor: colorScheme('rgb(255, 255, 228)'),
    directivesUniversityFillColor: colorScheme('rgb(255, 255, 228)'),
    directivesHospitalFillColor: colorScheme('rgb(255, 255, 228)'),
    directivesGraveYardFillColor: colorScheme('rgb(170, 203, 175)'),
    //Fountain
    directivesFountainFillColor: colorScheme('rgb(170, 211, 223)'),
    directivesFountainFillOutlineColor: colorScheme('rgb(170, 211, 223)'),
    //Overlay
    directivesMotorcycleParkingFillColor: colorScheme('rgb(238, 238, 238)'),
    directivesParkingFillColor: colorScheme('rgb(238, 238, 238)'),

    //Attraction 
    //style
    attractionWaterSlideLineColor: colorScheme('rgba(170, 224, 203, 1)'),

    //Background
    //style
    backgroundBackgroundColor: colorScheme('rgb(242, 239, 233)'),

    //Barrier
    //style
    barrierGuardRailLineColor: colorScheme('rgba(139, 177, 162, 1)'),

    //Boundary
    //line
    directivesAdminLevelLineColor: colorScheme('rgb(207, 155, 203)'),

    //Building
    //number
    buildingNumberTextColor: colorScheme('rgb(96,96,96)'),
    buildingNUmberTextHaloColor: colorScheme('rgba(255,255,255,0.8)'),
    //shape
    buildingFillColor: colorScheme('rgb(216, 208, 201)'),
    buildingFillOutlineColor: colorScheme('rgb(199, 185, 174)'),

    //Label
    //style
    dormitoryLabelsTextColor: colorScheme('rgba(0, 0, 0, 1)'),
    dormitoryLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    quarryLabelsTextColor: colorScheme('rgba(71, 69, 69, 1)'),
    quarryLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    buildingSchoolLabelsTextColor: colorScheme('rgba(0, 0, 0, 1)'),
    buildingSchoolLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    buildingLabelsTextColor: colorScheme('rgba(0, 0, 0, 1)'),
    buildingLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    socialFacilityLabelsTextColor: colorScheme('rgba(115, 74, 9, 1)'),
    socialFacilityLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    socialFacilityLabelsIconColor: colorScheme('rgba(115, 74, 9, 1)'),
    socialFacilityLabelsIconHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    pitchLabelsTextColor: colorScheme('rgba(77, 153, 122, 1)'),
    pitchLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    theatreLabelsTextColor: colorScheme('rgba(115, 74, 9, 1)'),
    theatreLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    theatreLabelsIconColor: colorScheme('rgba(115, 74, 9, 1)'),
    theatreLabelsIconHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    communityCentreLabelsTextColor: colorScheme( 'rgba(115, 74, 9, 1)'),
    communityCentreLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    communityCentreLabelsIconColor: colorScheme('rgba(115, 74, 9, 1)'),
    communityCentreLabelsIconHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    sportHallLabelsTextColor: colorScheme('rgba(103, 103, 102, 1)'),
    sportHallLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    schoolLabelsTextColor: colorScheme('rgba(101, 101, 41, 1)'),
    schoolLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    collegeLabelsTextColor: colorScheme('rgba(101, 101, 41, 1)'),
    collegeLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    courthouseLabelsTextColor: colorScheme('rgba(115, 74, 9, 1)'),
    courthouseLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    courthouseLabelsIconColor: colorScheme('rgba(115, 74, 9, 1)'),
    courthouseLabelsIconHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    parkingLabelsTextColor: colorScheme('rgba(33, 118, 254, 1)'),
    parkingLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    parkingLabelsIconColor: colorScheme('rgba(3, 146, 218, 1)'),
    parkingLabelsIconHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    industrialLabelsTextColor: colorScheme('rgba(149, 89, 139, 1)'),
    industrialLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poicafeTextColor: colorScheme('rgba(199, 116, 0, 1)'),
    poicafeIconColor: colorScheme('rgba(199, 116, 0, 1)'),
    poicafeTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poicafeIconHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poiAtmTextColor: colorScheme('rgba(115, 74, 10, 1)'),
    poiAtmTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poiAtmIconColor:colorScheme('rgba(115, 74, 10, 1)'),
    poiAtmIconHaloColor:colorScheme('rgba(255, 255, 255, 0.8)'),
    poiFuelChargingTextColor: colorScheme('rgba(0, 146, 218, 1)'),
    poiFuelChargingIconColor: colorScheme('rgba(0, 146, 218, 1)'),
    poiFuelChargingIconHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poiFuelChargingTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poiParkingTextColor: colorScheme('rgba(0, 146, 218, 1)'),
    poiParkingIconColor: colorScheme('rgba(0, 146, 218, 1)'),
    poiParkingIconHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poiParkingTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poiHotelTextColor: colorScheme('rgba(3, 146, 218, 1)'),
    poiHotelIconColor: colorScheme('rgba(3, 146, 218, 1)'),
    poiHotelIconHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poiHotelTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poiShopsTextColorLiteral: colorScheme('rgba(172, 57, 172, 1)'),
    poiShopsTextColorMassage: colorScheme('rgba(14, 133, 24, 1)'),
    poiShopsTextColor: colorScheme('rgba(199, 116, 0, 1)'),
    poiShopsIconColorLiteral: colorScheme('rgba(172, 57, 172, 1)'),
    poiShopsIconColorMassage: colorScheme('rgba(14, 133, 24, 1)'),
    poiShopsIconColor: colorScheme('rgba(199, 116, 0, 1)'),
    poiShopsIconHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poiShopsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poiRestaurantsTextColor: colorScheme('rgba(199, 116, 0, 1)'),
    poiRestaurantsIconColor: colorScheme('rgba(199, 116, 0, 1)'),
    poiReaturantsIconHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poiRestaurantsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poiFastFoodTextColor: colorScheme('rgba(199, 116, 0, 1)'),
    poiFastFoodIconColor: colorScheme('rgba(199, 116, 0, 1)'),
    poiFastFoodTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poiFastFoodIconHaloColor:colorScheme('rgba(255, 255, 255, 0.8)'),
    poiBarPubTextColor: colorScheme('rgba(199, 116, 0, 1)'),
    poiBarPubIconColor: colorScheme('rgba(199, 116, 0, 1)'),
    poiBarPubIconHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poiBarPubTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poiPharmacieTextColor:colorScheme('rgba(191, 0, 0, 1)'),
    poiPharmacieIconColor: colorScheme('rgba(191, 0, 0, 1)'),
    poiPharmacieIconHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    poiPharmacieTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    zooLabelsTextColor: colorScheme('rgba(102, 0, 51, 1)'),
    zooLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    monumentLabelsTextColor: colorScheme('rgba(115, 74, 9, 1)'),
    monumentLabelsIconColor:colorScheme('rgba(115, 74, 9, 1)'),
    monumentLabelsIconHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    monumentLabelsTextHaloColor:colorScheme('rgba(255, 255, 255, 0.8)'),
    cemeteryLabelsTextColor:colorScheme('rgba(69, 95, 72, 1)'),
    cemeteryLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    forestLabelsTextColor: colorScheme('rgba(89, 111, 82, 1)'),
    forestLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    stadiumLabelsTextColor: colorScheme('rgba(23, 117, 31, 1)'),
    stadiumLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    parkLabelsTextColor: colorScheme('rgba(31, 143, 40, 1)'),
    parkLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    hopitalLabelsTextColor: colorScheme('rgba(191, 3, 1, 1)'),
    hopitalLabelsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    hopitalLabelsIconColor: colorScheme('rgba(191, 3, 1, 1)'),
    hopitalLabelsIconHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    motorwayJunctionRefTextColor: colorScheme('rgba(179, 77, 73, 1)'),
    motorwayJunctionRefTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    motorwayJunctionNameTextColor: colorScheme('rgba(179, 77, 73, 1)'),
    motorwayJunctionNameTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),

    //Landuse
    //background
    directivesVillageGreenFillColor: colorScheme('rgb(205, 235, 176)'),
    directivesSaltPondFillColor: colorScheme('rgb(170, 211, 223)'),
    directivesReligiousFillColor: colorScheme('rgb(205, 204, 201)'),
    directivesRecreationGroundFillColor: colorScheme('rgb(223, 252, 226)'),
    directivesRailwayFillColor: colorScheme('rgb(236, 218, 233)'),
    directivesQuarryFillColor: colorScheme('rgb(195, 194, 194)'),
    directivesPlantNurseryFillColor: colorScheme('rgb(174, 223, 162)'),
    directivesMilitaryFillColor: colorScheme('rgb(242, 228, 221)'),
    directivesLandfillFillColor: colorScheme('rgb(182, 182, 144)'),
    directivesGreenfieldFillColor: colorScheme('rgb(242, 238, 232)'),
    directivesGaragesFillColor: colorScheme('rgb(222, 221, 204)'),
    directivesCemeteryFillColor: colorScheme('rgb(170, 203, 175)'),
    directivesBrowmfieldFillColor: colorScheme('rgb(182, 182, 144)'),
    directivesBasinFillColor: colorScheme('rgb(170, 211, 223)'),
    directivesVineyardFillColor: colorScheme('rgb(172, 225, 161)'),
    directivesMeadowFillColor: colorScheme('rgb(205, 235, 176)'),
    directivesFarmyardFillColor: colorScheme('rgb(238, 213, 179)'),
    directivesFarmlandFillColor: colorScheme('rgb(237, 240, 214)'),
    directivesAllotmentsFillColor: colorScheme('rgb(202, 224, 191)'),
    directivesRetailFillColor: colorScheme('rgb(254, 213, 208)'),
    directivesIndustrialFillColor: colorScheme('rgb(235, 219, 232)'),
    directivesResidentialFillColor: colorScheme('rgb(225, 225, 225)'),
    directivesConstructionFillColor: colorScheme('rgb(199, 199, 180)'),
    directivesCommercialFillColor: colorScheme('rgb(242, 216, 217)'),
    directivesPedestrianFillColor: colorScheme('rgb(221, 221, 233)'),
    //overlay
    directivesGrassFillColor: colorScheme('rgb(205, 235, 176)'),
    directivesForestFillColor: colorScheme('rgb(171, 210, 156)'),
    directivesGreenhouseHorticultureFillColor: colorScheme('rgb(237, 240, 214)'),
    directivesOrchardFillColor: colorScheme('rgb(172, 225, 161)'),
    //Point
    //Country_label
    countryLabelCountryTextColor: colorScheme('rgb(90, 56, 90)'),
    CountryLabelPaintTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    //Leisure
    //background
    directivesSwimmingPoolFillColor: colorScheme('rgb(170, 211, 223)'),
    directivesSwimmingPoolFillOutlineColor: colorScheme('rgb(120, 183, 202)'),
    directivesMiniatureGolfFillColor: colorScheme('rgb(181, 226, 181)'),
    directivesIceRinkFillColor: colorScheme('rgb(221, 236, 236)'),
    directivesIceRinkFillOutlineColor: colorScheme('rgb(140, 220, 189)'),
    directivesGolfCourseFillColor: colorScheme('rgb(181, 226, 181)'),
    directivesGardenFillColor: colorScheme('rgb(205, 235, 176)'),
    directivesDogParkFillColor: colorScheme('rgb(224, 252, 227)'),
    directivesPlayGroundFillColor: colorScheme('rgb(223, 252, 226)'),
    directivesPlayGroundFillOutlineColor: colorScheme('rgb(164, 221, 169)'),
    directivesPitchFillColor: colorScheme('rgb(170, 224, 203)'),
    directivesPitchFillOutlineColor: colorScheme('rgb(151, 212, 186)'),
    directivesTrackFillColor: colorScheme('rgb(196, 224, 203)'),
    directivesTrackFillOutlineColor: colorScheme('rgba(101, 206, 166, 1.0)'),
    directivesSportsCentreFillColor: colorScheme('rgb(223, 252, 226)'),
    directivesStadiumFillColor: colorScheme('rgb(223, 252, 226)'),
    directivesParkFillColor: colorScheme('rgb(200, 250, 204)'),
    //nature_reserve
    directivesNatureReserveLineColor: colorScheme('rgba(230, 233, 222, 0.5)'),
    //Overlay
    marinaFillColor: colorScheme('rgb(181, 208, 208)'),
    marinaFillOutlineColor: colorScheme('rgb(164, 187, 212)'),

    //ManMade
    //bridge
    directivesBridgeFillColor: colorScheme('rgb(184, 184, 184)'),
    // PierLabel
    manMadePierLabelTextHaloColor: colorScheme('rgba(255,255,255,0.8)'),
    //PierLine
    manMadePierLineLineColor: colorScheme('rgb(242, 239, 233)'),

    //natural
    //background
    directivesGlacierFillColor: colorScheme('rgb(221, 236, 236)'),
    directivesWoodFillColor: colorScheme('rgb(157, 202, 138)'),
    directivesHeathFillColor: colorScheme('rgb(214, 217, 159)'),
    directivesGrasslandFillColor: colorScheme('rgb(207, 236, 177)'),
    directivesBareRockFillColor: colorScheme('rgb(217, 212, 206)'),
    directivesScreeFillColor: colorScheme('rgb(232, 223, 216)'),
    directivesShingleFillColor: colorScheme('rgb(232, 223, 216)'),
    directivesWaterFillColor: colorScheme('rgb(170, 211, 223)'),
    //Overlay
    directivesBeachFillColor: colorScheme('rgb(255, 241, 186)'),
    directivesSandFillColor: colorScheme('rgb(240, 229, 196)'),
    directivesScrubFillColor: colorScheme('rgb(201, 216, 173)'),
    directivesLakeFillColor: colorScheme('rgb(170, 211, 223)'),
    directivesWetlandFillColor: colorScheme('rgb(213, 231, 211)'),
    //tree
    naturalTreeCircleColor: colorScheme('rgb(113, 205, 111)'),
    //trunk
    naturalTrunkCircleColor:colorScheme('rgb(129, 94, 39)'),
    //water
    naturalWaterFillColor: colorScheme('rgb(170, 211, 223)'),

    //Ocean
    //Overlay
    waterFillColor: colorScheme('rgb(170, 211, 223)'),

    //Point_label
    pointLabelCityLabelColor: colorScheme('rgb(25, 25, 25)'),
    pointLabelCityFilterOneLabelColor: colorScheme('rgb(100, 100, 100)'),
    pointLabelCityFilterTwoLabelColor: colorScheme('rgb(50, 50, 50)'),
    pointLabelTownFilterOneLabelColor: colorScheme('rgb(100, 100, 100)'),
    pointLabelTownFilterTwoLabelColor: colorScheme('rgb(75, 75, 75)'),
    pointLabelVillageLabelColor: colorScheme('rgb(100, 100, 100)'),
    pointLabelLocalityLabelColor: colorScheme('rgb(100, 100, 100)'),
    pointLabelPlaceTextColor: colorScheme('rgba(100, 100, 100, 1)'),
    pointLabelPaintTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    //Point
    //Icon
    directivesBarIconColor: colorScheme('rgb(199, 116, 0)'),
    directivesBarTextColor: colorScheme('rgb(199, 116, 0)'),
    directivesBiergartenIconColor: colorScheme('rgb(199, 116, 0)'),
    directivesBiergartenTextColor: colorScheme('rgb(199, 116, 0)'),
    directivesCafeIconColor: colorScheme('rgb(199, 116, 0)'),
    directivesCafeTextColor: colorScheme('rgb(199, 116, 0)'),
    directivesFastFoodIconColor: colorScheme('rgb(199, 116, 0)'),
    directivesFastFoodTextColor: colorScheme('rgb(199, 116, 0)'),
    directivesFoodCourtIconColor:colorScheme('rgb(199, 116, 0)'),
    directivesFoodCourtTextColor: colorScheme('rgb(199, 116, 0)'),
    directivesIceCreamIconColor: colorScheme('rgb(199, 116, 0)'),
    directivesIceCreamTextColor: colorScheme('rgb(199, 116, 0)'),
    directivesPubIconColor:colorScheme('rgb(199, 116, 0)'),
    directivesPubTextColor: colorScheme('rgb(199, 116, 0)'),
    directivesRestaurantIconColor: colorScheme('rgb(199, 116, 0)'),
    directivesRestaurantTextColor: colorScheme('rgb(199, 116, 0)'),
    directivesAmenityEducationDrivingSchoolIconColor: colorScheme('rgb(172, 58, 173)'),
    directivesAmenityEducationDrivingSchoolTextColor: colorScheme('rgb(172, 58, 173)'),
    directivesLibraryIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesLibraryTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesBicycleParkingIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesBicycleParkingTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesBicycleRepairStationIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesBicycleRepairStationTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesBicycleRentalIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesBicycleRentalTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesBoatRentalIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesBoatRentalTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesBusStationIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesBusStationTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesCarRentalIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesCarRentalTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesCarWashIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesCarWashTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesVehicleInspectionIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesVehicleInspectionTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesChargingStationIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesChargingStationTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesFerryTerminalIconColor: colorScheme('rgb(132, 97, 196)'),
    directivesFerryTerminalTextColor: colorScheme('rgb(132, 97, 196)'),
    directivesFuelIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesFuelTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesMotorcycleParkingIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesMotorcycleParkingTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesParkingIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesParkingTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesParkingEntranceIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesParkingEntranceTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesTaxiIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesTaxiTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesBusStopIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesBusStopTextColo: colorScheme('rgb(0, 146, 219)'),
    directivesAtmIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesAtmTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesBankIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesBankTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesBureauDeChangeIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesBureauDeChangeTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesCliniqueIconColor: colorScheme('rgb(191, 0, 0)'),
    directivesCliniqueTextColor: colorScheme('rgb(191, 0, 0)'),
    directivesDentistIconColor: colorScheme('rgb(191, 0, 0)'),
    directivesDentistTextColor: colorScheme('rgb(191, 0, 0)'),
    directivesDoctorIconColor: colorScheme('rgb(191, 0, 0)'),
    directivesDoctorTextColor:colorScheme('rgb(191, 0, 0)'),
    directivesHospitalIconColor: colorScheme('rgb(191, 0, 0)'),
    directivesHospitalTextColor: colorScheme('rgb(191, 0, 0)'),
    directivesNursingHomeIconColor: colorScheme('rgb(76, 76, 0)'),
    directivesNursingHomeTextColor: colorScheme('rgb(76, 76, 0)'),
    directivesPharmacieIconColor: colorScheme('rgb(191, 0, 0)'),
    /////////////////////////////////////
    directivesPharmacieTextColor: colorScheme('rgb(191, 0, 0)'),
    directivesSocialFacilityIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesSocialFacilityTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesVeterinaryIconColor: colorScheme('rgb(191, 0, 0)'),
    directivesVeterinaryTextColor: colorScheme('rgb(191, 0, 0)'),
    directivesArtsCentreIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesArtsCentreTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesCasinoIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesCasinoTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesCinemaIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesCinemaTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesCommunityCentreIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesCommunityCentreTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesFountainIconColor: colorScheme('rgb(87, 104, 236)'),
    directivesFountainTextColor: colorScheme('rgb(87, 104, 236)'),
    directivesNigthclubIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesNightclubTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesPublicBookcaseIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesPublicBookCaseTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesTheatreIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesTheatreTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesCourthouseIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesCourthouseTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesFireStationIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesFireStationTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesPoliceIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesPoliceTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesPostBoxIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesPostBoxTextColor:colorScheme('rgb(115, 74, 7)'),
    directivesPostOfficeIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesPostOfficeTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesPrisonIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesPrisonTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesTownhallIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesTowmhallTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesBbqIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesBbqTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesBenchIconColor: colorScheme('rgb(102, 102, 102)'),
    directivesBenchTextColor: colorScheme('rgb(102, 102, 102)'),
    directivesDrinkingWaterIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesDrinkingWaterTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesShelterIconColor: colorScheme('rgb(102, 102, 102)'),
    directivesShelterTextColor: colorScheme('rgb(102, 102, 102)'),
    directivesShowerIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesShowerTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesTelephoneIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesTelephoneTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesToiletsIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesToiletsTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesRecyclingIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesRecyclingTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesWasteBasketIconColor: colorScheme('rgb(102, 102, 102)'),
    directivesWasteBasketTextColor: colorScheme('rgb(102, 102, 102)'),
    directivesWasteDisposalIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesWasteDisposalTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesChildcareIconColor: colorScheme('rgb(76, 76, 0)'),
    directivesChildcareTextColor: colorScheme('rgb(76, 76, 0)'),
    directivesHuntingStandIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesHuntingStandTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesInternetCafeIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesInternetCafeTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesMarketplaceIconColor: colorScheme('rgb(172, 58, 173)'),
    directivesMarketplaceTextColor: colorScheme('rgb(172, 58, 173)'),
    directivesPlaceOfWorkshipIconColor: colorScheme('rgb(0, 0, 0)'),
    directivesPlaceOfWorkshipTextColor: colorScheme('rgb(0, 0, 0)'),
    directivesPublicBathIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesPublicBathTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesArchaeologicalSiteIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesArchaeologicalSiteTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesCastleIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesCastleTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesCityGateIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesCityGateTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesFortIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesFortTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesManorIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesManorTextColor:colorScheme('rgb(115, 74, 7)'),
    directivesMemorialIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesMemorialTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesMonumentIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesMonumentTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesWaysideCrossIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesWaysideCrossTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesWayShrineIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesWaysideShrineTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesAmusementArcadeIconColor: colorScheme('rgb(13, 134, 22)'),
    directivesAmusementArcadeTextColor: colorScheme('rgb(13, 134, 22)'),
    directivesBeachResortIconColor: colorScheme('rgb(13, 134, 22)'),
    directivesBeachResortTextColor: colorScheme('rgb(13, 134, 22)'),
    directivesBirdHideIconColor: colorScheme('rgb(13, 134, 22)'),
    directivesBirdHideTextColor: colorScheme('rgb(13, 134, 22)'),
    directivesBowlingAlleyIconColor: colorScheme('rgb(13, 134, 22)'),
    directivesBowlingAlleyTextColor: colorScheme('rgb(13, 134, 22)'),
    directivesFirepitIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesFirepitTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesFishingIconColor: colorScheme('rgb(13, 134, 22)'),
    directivesFishingTextColor: colorScheme('rgb(13, 134, 22)'),
    directivesFitnessCentreIconColor: colorScheme('rgb(13, 134, 22)'),
    directivesFitnessCentreTextColor: colorScheme('rgb(13, 134, 22)'),
    directivesFitnessStationIconColor: colorScheme('rgb(13, 134, 22)'),
    directivesFitnessStationTextColor: colorScheme('rgb(13, 134, 22)'),
    directivesGolfCourseIconColor: colorScheme('rgb(13, 134, 22)'),
    directivesGolfCourseTextColor: colorScheme('rgb(13, 134, 22)'),
    directivesMiniatureGolfIconColor: colorScheme('rgb(13, 134, 22)'),
    directivesMiniatureGolfTextColor: colorScheme('rgb(13, 134, 22)'),
    directivesOutdoorSeatingIconColor: colorScheme('rgb(13, 134, 22)'),
    directivesOutdoorSeatingTextColor: colorScheme('rgb(13, 134, 22)'),
    directivesPicnicTableIconColor: colorScheme('rgb(102, 102, 102)'),
    directivesPicnicTableTextColor: colorScheme('rgb(102, 102, 102)'),
    directivesPlaygroundIconColor: colorScheme('rgb(13, 134, 22)'),
    directivesPlaygroundTextColor: colorScheme('rgb(13, 134, 22)'),
    directivesSaunaIconColor: colorScheme('rgb(13, 134, 22)'),
    directivesSaunaTextColor: colorScheme('rgb(13, 134, 22)'),
    directivesSlipwayIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesSlipwayTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesSwimmingAreaIconColor: colorScheme('rgb(13, 134, 22)'),
    directivesSwimmingAreaTextColor: colorScheme('rgb(13, 134, 22)'),
    directivesWaterParkIconColor: colorScheme('rgb(13, 134, 22)'),
    directivesWaterParkTextColor: colorScheme('rgb(13, 134, 22)'),
    directivesChimneyIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesChimneyTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesCommunicationTowerIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesCommunicationTowerTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesCraneIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesCraneTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesCrossIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesCrossTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesLighthouseIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesLightHouseTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesMastIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesMastTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesObeliskIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesObeliskTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesSiloIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesSiloTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesStorageTankIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesStorageTankTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesTelescopeIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesTelescopeTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesTowerIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesTowerTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesWaterTowerIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesWaterTowerTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesWindmillIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesWindmillTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesBunkerIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesBunkerTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesSpringIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesSpringTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesCaveEntranceIconColor: colorScheme('rgb(85, 85, 85)'),
    directivesCaveEntranceTextColor: colorScheme('rgb(85, 85, 85)'),
    directivesPeakIconColor: colorScheme('rgb(209, 144, 85)'),
    directivesPeakTextColor: colorScheme('rgb(209, 144, 85)'),
    directivesSaddleIconColor: colorScheme('rgb(209, 144, 85)'),
    directivesSaddleTextColor: colorScheme('rgb(209, 144, 85)'),
    directivesVolcanoIconColor: colorScheme('rgb(212, 0, 0)'),
    directivesVolcanoTextColor: colorScheme('rgb(212, 0, 0)'),
    directivesHaltIconColor: colorScheme('rgb(122, 129, 177)'),
    directivesHaltTextColor: colorScheme('rgb(122, 129, 177)'),
    directivesStationIconColor: colorScheme('rgb(122, 129, 177)'),
    directivesStationTextColor: colorScheme('rgb(122, 129, 177)'),
    directivesSubwayEntranceIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesSubwayEntranceTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesTramStopIconColor: colorScheme('rgb(122, 129, 177)'),
    directivesTramStopTextColor: colorScheme('rgb(122, 129, 177)'),
    directivesCrossingIconColor: colorScheme('rgb(102, 102, 102)'),
    directivesCrossingTextColor: colorScheme('rgb(102, 102, 102)'),
    directivesLevelCrossingIconColor: colorScheme('rgb(102, 102, 102)'),
    directivesLevelCrossingTextColor: colorScheme('rgb(102, 102, 102)'),
    directivesAlpineHutIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesAlpineHutTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesApartmentIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesApartmentTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesArtworkIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesArtworkTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesCampSiteIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesCampSiteTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesCaravaneSiteIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesCaravaneSiteTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesChaletIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesChaletTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesGalleryIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesGalleryTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesGuestHouseIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesGuestHouseextColor: colorScheme('rgb(0, 146, 219)'),
    directivesHostelIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesHostelTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesMotelIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesMotelTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesMuseumIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesMuseumTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesPicnicSiteIconColor: colorScheme('rgb(102, 102, 102)'),
    directivesPicnicSiteTextColor: colorScheme('rgb(102, 102, 102)'),
    directivesViewpointIconColor: colorScheme('rgb(115, 74, 7)'),
    directivesViewpointTextColor: colorScheme('rgb(115, 74, 7)'),
    directivesWildernessHutIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesWildernessHutTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesDamIconColor: colorScheme('rgb(173, 173, 173)'),
    directivesDamTextColor: colorScheme('rgb(173, 173, 173)'),
    directivesWeirdIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesWeirdTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesWaterfallIconColor: colorScheme('rgb(0, 146, 219)'),
    directivesWaterfallTextColor: colorScheme('rgb(0, 146, 219)'),
    directivesLockGateIconColor: colorScheme('rgb(173, 173, 173)'),
    directivesLockGateTextColor: colorScheme('rgb(173, 173, 173)'),
    iconsIconHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    iconsTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    //Label
    directivesYesTextColor: colorScheme('rgb(25, 25, 25)'),
    directivesCityTextColor: colorScheme('rgb(25, 25, 25)'),
    directivesTownTextColor: colorScheme('rgb(50,50,50)'),
    directivesVillageTextColor: colorScheme('rgb(75,75,75)'),
    directivesLocalityTextColor: colorScheme('rgb(75,75,75)'),
    labelTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),

    //power
    //background
    backgroundPowerPlantFillColor: colorScheme('rgb(226, 203, 222)'),
    backgroundPowerPlantFillOutlineColor: colorScheme('rgb(171, 171, 171)'),
    //cable
    powerCableLineColor: colorScheme('rgb(171, 171, 171)'),
    //Tower
    powerTowerCircleColor: colorScheme('rgb(171, 171, 171)'),

    //Railway
    //Line
    directivesRailLineColor: colorScheme('rgb(112,112,112)'),
    directivesAllRailsLineColor: colorScheme('rgb(160,160,160)'),
    directivesSubwayLineColor: colorScheme('rgb(160,160,160)'),
    directivesTramLineColor: colorScheme('rgb(77,77,77)'),
    directivesPreservedLineColor: colorScheme('rgb(220,220,220)'),
    directivesFunicularLineColor: colorScheme('rgb(100,100,100)'),
    directivesMonorailLineColor: colorScheme('rgb(126,126,126)'),
    directivesLigthRailLineColor: colorScheme('rgb(100,100,100)'),
    directivesConstructionLineColor: colorScheme('rgb(170,170,170)'),
    directivesAbandonedLineColor: colorScheme('rgb(100,100,100)'),
    directivesDisuedLineColor: colorScheme('rgb(100,100,100)'),
    directivesMiniatureLineColor: colorScheme('rgb(158,158,158)'),
    directivesMarrowGaugeLineColor: colorScheme('rgb(100,100,100)'),
    
    //Route
    //Style
    directivesFerryLineColor: colorScheme('rgb(112, 181, 201)'),

    //Tourism
    //styleZooFill
    tourismZooCasingLineColor: colorScheme("rgba(182, 145, 156, 1)"),
    //styleZooLine
    tourismZooLineColor: colorScheme("rgba(145, 79, 107, 1)"),

    //waterway
    //Label
    waterwayLabelTextColor: colorScheme('rgba(26, 109, 187, 1)'),
    waterwayLabelTextHaloColor: colorScheme('rgba(255, 255, 255, 0.8)'),
    //line
    waterwayLineLineColor: colorScheme('rgb(170, 211, 223)'),
    //tunnelCasing
    waterwayTunnelCasingLineColor: colorScheme('rgb(170, 211, 223)'),
    //TunnelLine
    waterwayTunnelLineLineColor: colorScheme('rgb(243, 247, 247)')


};