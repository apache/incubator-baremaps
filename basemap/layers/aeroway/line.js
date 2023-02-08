import {asLayerObject, withSortKeys} from "../../utils/utils.js";

let directives = [
    {
        'filter': ['==', ['get', 'aeroway'], 'runway'],
        'line-color': 'rgba(187, 187, 204, 1.0)',
        'road-width': 30,
    },
    {
        'filter': ['==', ['get', 'aeroway'], 'taxiway'],
        'line-color': 'rgba(187, 187, 204, 1.0)',
        'road-width': 14,
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'aeroway_line',
    type: 'line',
    source: 'baremaps',
    'source-layer': 'aeroway',
    filter: ['==', ['geometry-type'], 'LineString'],
    layout: {
        'line-cap': 'round',
        'line-join': 'round',
        visibility: 'visible',
    },
});
