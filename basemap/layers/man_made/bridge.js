import {asLayerObject} from "../../utils/utils.js";

let directives = [
    {
        filter: ['==', ['get', 'man_made'], 'bridge'],
        'fill-color': 'rgb(184, 184, 184)',
    },
];

export default asLayerObject(directives, {
    id: 'man_made_bridge',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'man_made',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': true,
    },
});
