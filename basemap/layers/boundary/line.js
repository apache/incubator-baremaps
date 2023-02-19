import {asLayerObject, withSortKeys} from "../../utils/utils.js";

let directives = [
    {
        filter: ['==', ['get', 'admin_level'], "0"],
        'line-color': 'rgb(207, 155, 203)',
    },
    {
        filter: ['==', ['get', 'admin_level'], "1"],
        'line-color': 'rgb(207, 155, 203)',
    },
    {
        filter: ['==', ['get', 'admin_level'], "2"],
        'line-color': 'rgb(207, 155, 203)',
    },
    {
        filter: ['==', ['get', 'admin_level'], "3"],
        'line-color': 'rgb(207, 155, 203)',
    },
    {
        filter: ['==', ['get', 'admin_level'], "4"],
        'line-color': 'rgb(207, 155, 203)',
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'boundary',
    type: 'line',
    source: 'baremaps',
    'source-layer': 'boundary',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'line-dasharray': [4, 1, 1, 1],
    },
});
