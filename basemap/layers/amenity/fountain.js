import {asLayerObject} from "../../utils/utils.js";

let directives = [
    {
        filter: ['==', ['get', 'amenity'], 'fountain'],
        'fill-color': 'rgb(170, 211, 223)',
        'fill-outline-color': 'rgb(170, 211, 223)',
    },
];

export default asLayerObject(directives, {
    id: 'amenity_fill_2',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'amenity',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': true,
    },
});
