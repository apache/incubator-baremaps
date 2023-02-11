import {withFillSortKey} from "../../utils/utils.js";

let directives = [
    {
        filter: ['==', ['get', 'leisure'], 'marina'],
        'fill-color': 'rgb(181, 208, 208)',
        'fill-outline-color': 'rgb(164, 187, 212)',
    },
];

export default {
    id: 'leisure_overlay',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'leisure',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': true,
    },
    directives: directives.map(withFillSortKey),
}
