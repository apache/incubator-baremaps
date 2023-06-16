/**
 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License
 is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 or implied. See the License for the specific language governing permissions and limitations under
 the License.
 **/
import {withFillSortKey, asLayoutProperty, asPaintProperty} from "../../utils/utils.js";
import theme from "../../theme.js";

let directives = [
    {
        filter: ['==', ['get', 'landuse'], 'village_green'],
        'fill-color': theme.landuseBackgroundVillageGreenFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'salt_pond'],
        'fill-color': theme.landuseBackgroundSaltPondFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'religious'],
        'fill-color': theme.landuseBackgroundReligiousFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'recreation_ground'],
        'fill-color': theme.landuseBackgroundRecreationGroundFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'railway'],
        'fill-color': theme.landuseBackgroundRailwayFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'quarry'],
        'fill-color': theme.landuseBackgroundQuarryFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'plant_nursery'],
        'fill-color': theme.landuseBackgroundPlantNurseryFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'military'],
        'fill-color': theme.landuseBackgroundMilitaryFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'landfill'],
        'fill-color': theme.landuseBackgroundLandfillFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'greenfield'],
        'fill-color': theme.landuseBackgroundGreenfieldFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'garages'],
        'fill-color': theme.landuseBackgroundGaragesFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'cemetery'],
        'fill-color': theme.landuseBackgroundCemeteryFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'brownfield'],
        'fill-color': theme.landuseBackgroundBrowmfieldFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'basin'],
        'fill-color': theme.landuseBackgroundBasinFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'vineyard'],
        'fill-color': theme.landuseBackgroundVineyardFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'farmyard'],
        'fill-color': theme.landuseBackgroundFarmyardFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'farmland'],
        'fill-color': theme.landuseBackgroundFarmlandFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'allotments'],
        'fill-color': theme.landuseBackgroundAllotmentsFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'retail'],
        'fill-color': theme.landuseBackgroundRetailFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'industrial'],
        'fill-color': theme.landuseBackgroundIndustrialFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'residential'],
        'fill-color': theme.landuseBackgroundResidentialFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'construction'],
        'fill-color': theme.landuseBackgroundConstructionFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'commercial'],
        'fill-color': theme.landuseBackgroundCommercialFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'pedestrian'],
        'fill-color': theme.landuseBackgroundPedestrianFillColor,
    },
].map(withFillSortKey);

export default {
    id: 'landuse_background',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'landuse',
    layout: asLayoutProperty(directives, {
        visibility: 'visible',
    }),
    paint: asPaintProperty(directives, {
        'fill-antialias': true,
    }),
}
