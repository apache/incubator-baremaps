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
        'fill-color': theme.directivesVillageGreenFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'salt_pond'],
        'fill-color': theme.directivesSaltPondFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'religious'],
        'fill-color': theme.directivesReligiousFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'recreation_ground'],
        'fill-color': theme.directivesRecreationGroundFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'railway'],
        'fill-color': theme.directivesRailwayFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'quarry'],
        'fill-color': theme.directivesQuarryFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'plant_nursery'],
        'fill-color': theme.directivesPlantNurseryFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'military'],
        'fill-color': theme.directivesMilitaryFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'landfill'],
        'fill-color': theme.directivesLandfillFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'greenfield'],
        'fill-color': theme.directivesGreenfieldFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'garages'],
        'fill-color': theme.directivesGaragesFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'cemetery'],
        'fill-color': theme.directivesCemeteryFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'brownfield'],
        'fill-color': theme.directivesBrowmfieldFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'basin'],
        'fill-color': theme.directivesBasinFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'vineyard'],
        'fill-color': theme.directivesVineyardFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'meadow'],
        'fill-color': theme.directivesMeadowFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'farmyard'],
        'fill-color': theme.directivesFarmyardFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'farmland'],
        'fill-color': theme.directivesFarmlandFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'allotments'],
        'fill-color': theme.directivesAllotmentsFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'retail'],
        'fill-color': theme.directivesRetailFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'industrial'],
        'fill-color': theme.directivesIndustrialFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'residential'],
        'fill-color': theme.directivesResidentialFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'construction'],
        'fill-color': theme.directivesConstructionFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'commercial'],
        'fill-color': theme.directivesCommercialFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'pedestrian'],
        'fill-color': theme.directivesPedestrianFillColor,
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
