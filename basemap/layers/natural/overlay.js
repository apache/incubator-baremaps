import {asLayerObject, withSortKeys} from "../../utils/utils.js";

let directives = [
    {
        filter: ['==', ['get', 'natural'], 'beach'],
        'fill-color': 'rgb(255, 241, 186)'
    },
    {
        filter: ['==', ['get', 'natural'], 'sand'],
        'fill-color': 'rgb(240, 229, 196)'
    },
    {
        filter: ['==', ['get', 'natural'], 'scrub'],
        'fill-color': 'rgb(201, 216, 173)'
    },
    {
        filter: [
            'all',
            ['==', ['get', 'natural'], 'water'],
            ['!=', ['get', 'water'], 'lake'],
        ],
        'fill-color': 'rgb(170, 211, 223)',
    },
    {
        filter: ['==', ['get', 'natural'], 'wetland'],
        'fill-color': 'rgb(213, 231, 211)'
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'natural_overlay',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'natural',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': true,
    },
});
