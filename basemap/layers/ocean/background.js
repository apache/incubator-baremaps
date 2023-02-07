import {withFillSortKey} from "../../utils/utils.js";

let directives = [
    {
        filter: ['==', ['get', 'ocean'], 'water'],
        'fill-color': 'rgb(170, 211, 223)',
        'fill-sort-key': 10,
    },
];

export default {
    id: 'ocean',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'ocean',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': true,
    },
    directives: directives.map(withFillSortKey),
}
