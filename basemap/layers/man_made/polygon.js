import {withFillSortKey} from "../../utils/utils.js";

let directives = [
    {
        filter: ['==', ['get', 'man_made'], 'bridge'],
        'fill-color': 'rgb(184, 184, 184)',
    },
];

export default {
    id: 'man_made_bridge',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'man_made',
    layout: {
        visibility: 'visible',
    },
    directives: directives.map(withFillSortKey),
    paint: {
        'fill-antialias': true,
    },
}
