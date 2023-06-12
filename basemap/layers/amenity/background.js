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
    {
        filter: ['==', ['get', 'amenity'], 'kindergarten'],
        'fill-color': colorScheme.amenityBackgroundKinderGartenFillColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'school'],
        'fill-color': colorScheme.amenityBackgroundSchoolFillColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'college'],
        'fill-color': colorScheme.amenityBackgroundCollegeFillColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'university'],
        'fill-color': colorScheme.amenityBackgroundUniversityFillColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'hospital'],
        'fill-color': colorScheme.amenityBackgroundHospitalFillColor,
    },
    {
        filter: ['==', ['get', 'amenity'], 'grave_yard'],
        'fill-color': colorScheme.amenityBackgroundGraveYardFillColor,
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'amenity_background',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'amenity',
});
