import {asLayerObject, withSortKeys} from "../../utils/utils.js";

let directives = [
    {
        filter: ['==', ['geometry-type'], 'Polygon'],
        'fill-color': 'rgba(187, 187, 204, 1.0)',
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'aeroway_polygon',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'aeroway',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': true,
    },
});
