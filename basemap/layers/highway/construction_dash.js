/**
 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License
 is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 or implied. See the License for the specific language governing permissions and limitations under
 the License.
 **/
 import theme from "../../theme.js";
import {asLayerObject, withSortKeys} from "../../utils/utils.js";


 let directives =[
     {
         filter: [
             'all',
             ['==', ['get', 'highway'], 'construction'],
             ['==', ['get', 'construction'], 'motorway'],
         ],
         'line-color': theme.constructionDashConstructionMotorwayLineColor,
         'road-width': 16,
     },
     {
         filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['==', ['get', 'construction'], 'trunk'],
         ],
         'line-color': theme.constructionDashConstructionTrunkLineColor,
         'road-width': 12,
     },
     {
         filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['==', ['get', 'construction'], 'primary'],
         ],
         'line-color': theme.constructionDashConstructionPrimaryLineColor,
         'road-width': 14,
     },
     {
         filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['==', ['get', 'construction'], 'secondary'],
         ],
         'line-color': theme.constructionDashConstructionSecondaryLineColor,
         'road-width': 12,
     },
     {
         filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['==', ['get', 'construction'], 'tertiary'],
         ],
         'line-color': theme.constructionDashConstructionTertiaryLineColor,
         'road-width': 12,
     },
     {
         filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['==', ['get', 'construction'], 'unclassified'],
         ],
         'line-color': theme.constructionDashConstructionUnclassifiedLineColor,
         'road-width': 8,
     },
     {
         filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['==', ['get', 'construction'], 'residential'],
         ],
         'line-color': theme.constructionDashConstructionResidentialLineColor,
         'road-width': 8,
     },
     {
         filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['==', ['get', 'construction'], 'living_street'],
         ],
         'line-color': theme.constructionDashConstructionLivingStreetLineColor,
         'road-width': 8,
     },
     {
         filter: [
            'all',
            ['==', ['get', 'highway'], 'construction'],
            ['==', ['get', 'construction'], 'service'],
         ],
         'line-color': theme.constructionDashConstructionServiceLineColor,
         'road-width': 8,
     },
 ]
 
 export default asLayerObject(withSortKeys(directives), {
     id: 'highway_construction_outline',
     source: 'baremaps',
     'source-layer': 'highway',
     type: 'line',
     layout: {
         visibility: 'visible',
         'line-cap': 'round',
         'line-join': 'round',
     },
 });
