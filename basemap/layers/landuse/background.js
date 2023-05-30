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
import colorScheme from "../../colorScheme.js";

let directives = [
    {
        filter: ['==', ['get', 'landuse'], 'village_green'],
        'fill-color': colorScheme.directivesVillageGreenFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'salt_pond'],
        'fill-color': colorScheme.directivesSaltPondFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'religious'],
        'fill-color': colorScheme.directivesReligiousFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'recreation_ground'],
        'fill-color': colorScheme.directivesRecreationGroundFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'railway'],
        'fill-color': colorScheme.directivesRailwayFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'quarry'],
        'fill-color': colorScheme.directivesQuarryFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'plant_nursery'],
        'fill-color': colorScheme.directivesPlantNurseryFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'military'],
        'fill-color': colorScheme.directivesMilitaryFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'landfill'],
        'fill-color': colorScheme.directivesLandfillFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'greenfield'],
        'fill-color': colorScheme.directivesGreenfieldFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'garages'],
        'fill-color': colorScheme.directivesGaragesFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'cemetery'],
        'fill-color': colorScheme.directivesCemeteryFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'brownfield'],
        'fill-color': colorScheme.directivesBrowmfieldFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'basin'],
        'fill-color': colorScheme.directivesBasinFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'vineyard'],
        'fill-color': colorScheme.directivesVineyardFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'meadow'],
        'fill-color': colorScheme.directivesMeadowFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'farmyard'],
        'fill-color': colorScheme.directivesFarmyardFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'farmland'],
        'fill-color': colorScheme.directivesFarmlandFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'allotments'],
        'fill-color': colorScheme.directivesAllotmentsFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'retail'],
        'fill-color': colorScheme.directivesRetailFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'industrial'],
        'fill-color': colorScheme.directivesIndustrialFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'residential'],
        'fill-color': colorScheme.directivesResidentialFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'construction'],
        'fill-color': colorScheme.directivesConstructionFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'commercial'],
        'fill-color': colorScheme.directivesCommercialFillColor,
    },
    {
        filter: ['==', ['get', 'landuse'], 'pedestrian'],
        'fill-color': colorScheme.directivesPedestrianFillColor,
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
