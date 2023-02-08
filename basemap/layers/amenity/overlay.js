import {asLayerObject, withSortKeys} from "../../utils/utils.js";

let directives = [
    {
        filter: ['==', ['get', 'amenity'], 'motorcycle_parking'],
        'fill-color': 'rgb(238, 238, 238)',
    },
    {
        filter: ['==', ['get', 'amenity'], 'parking'],
        'fill-color': 'rgb(238, 238, 238)',
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'amenity_overlay',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'amenity',
});
