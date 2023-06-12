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
import colorScheme from "../../theme.js";

let directives = [
    {
        filter: ['==', ['get', 'landuse'], 'village_green'],
        'fill-color': colorScheme.landuseBackgroundVillageGreenFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'salt_pond'],
        'fill-color': colorScheme.landuseBackgroundSaltPondFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'religious'],
        'fill-color': colorScheme.landuseBackgroundReligiousFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'recreation_ground'],
        'fill-color': colorScheme.landuseBackgroundRecreationGroundFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'railway'],
        'fill-color': colorScheme.landuseBackgroundRailwayFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'quarry'],
        'fill-color': colorScheme.landuseBackgroundQuarryFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'plant_nursery'],
        'fill-color': colorScheme.landuseBackgroundPlantNurseryFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'military'],
        'fill-color': colorScheme.landuseBackgroundMilitaryFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'landfill'],
        'fill-color': colorScheme.landuseBackgroundLandfillFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'greenfield'],
        'fill-color': colorScheme.landuseBackgroundGreenfieldFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'garages'],
        'fill-color': colorScheme.landuseBackgroundGaragesFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'cemetery'],
        'fill-color': colorScheme.landuseBackgroundCemeteryFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'brownfield'],
        'fill-color': colorScheme.landuseBackgroundBrowmfieldFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'basin'],
        'fill-color': colorScheme.landuseBackgroundBasinFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'vineyard'],
        'fill-color': colorScheme.landuseBackgroundVineyardFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'meadow'],
        'fill-color': colorScheme.landuseBackgroundMeadowFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'farmyard'],
        'fill-color': colorScheme.landuseBackgroundFarmyardFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'farmland'],
        'fill-color': colorScheme.landuseBackgroundFarmlandFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'allotments'],
        'fill-color': colorScheme.landuseBackgroundAllotmentsFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'retail'],
        'fill-color': colorScheme.landuseBackgroundRetailFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'industrial'],
        'fill-color': colorScheme.landuseBackgroundIndustrialFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'residential'],
        'fill-color': colorScheme.landuseBackgroundResidentialFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'construction'],
        'fill-color': colorScheme.landuseBackgroundConstructionFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'commercial'],
        'fill-color': colorScheme.landuseBackgroundCommercialFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'pedestrian'],
        'fill-color': colorScheme.landuseBackgroundPedestrianFillColor,
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
