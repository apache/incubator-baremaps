import {asLayerObject, withSortKeys} from "../../utils/utils.js";

let directives = [
    {
        filter: ['==', ['get', 'natural'], 'glacier'],
        'fill-color': 'rgb(221, 236, 236)'
    },
    {
        filter: ['==', ['get', 'natural'], 'wood'],
        'fill-color': 'rgb(157, 202, 138)'
    },
    {
        filter: ['==', ['get', 'natural'], 'heath'],
        'fill-color': 'rgb(214, 217, 159)'
    },
    {
        filter: ['==', ['get', 'natural'], 'grassland'],
        'fill-color': 'rgb(207, 236, 177)'
    },
    {
        filter: ['==', ['get', 'natural'], 'bare_rock'],
        'fill-color': 'rgb(217, 212, 206)'
    },
    {
        filter: ['==', ['get', 'natural'], 'scree'],
        'fill-color': 'rgb(232, 223, 216)'
    },
    {
        filter: ['==', ['get', 'natural'], 'shingle'],
        'fill-color': 'rgb(232, 223, 216)'
    },
    {
        filter: [
            'all',
            ['==', ['get', 'natural'], 'water'],
            ['==', ['get', 'water'], 'lake'],
        ],
        'fill-color': 'rgb(170, 211, 223)',
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'natural',
    type: 'fill',
    //filter: ['==', ['get', 'type'], 'Polygon'],
    source: 'baremaps',
    'source-layer': 'natural',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': true,
    },
});
