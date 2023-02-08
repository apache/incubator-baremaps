import {asLayerObject, withSortKeys} from "../../utils/utils.js";

let directives = [
    {
        filter: ['==', ['get', 'amenity'], 'kindergarten'],
        'fill-color': 'rgb(255, 255, 228)',
    },
    {
        filter: ['==', ['get', 'amenity'], 'school'],
        'fill-color': 'rgb(255, 255, 228)',
    },
    {
        filter: ['==', ['get', 'amenity'], 'college'],
        'fill-color': 'rgb(255, 255, 228)',
    },
    {
        filter: ['==', ['get', 'amenity'], 'university'],
        'fill-color': 'rgb(255, 255, 228)',
    },
    {
        filter: ['==', ['get', 'amenity'], 'hospital'],
        'fill-color': 'rgb(255, 255, 228)',
    },
    {
        filter: ['==', ['get', 'amenity'], 'grave_yard'],
        'fill-color': 'rgb(170, 203, 175)',
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'amenity_background',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'amenity',
});
