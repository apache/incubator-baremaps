import {withFillSortKey} from "../../utils/utils.js";

let directives =  [
    {
        filter: ['==', 'leisure', 'nature_reserve'],
        'line-width': 5,
        'line-color': 'rgba(230, 233, 222, 0.5)',
    },
];

export default {
    id: 'leisure_nature_reserve',
    type: 'line',
    source: 'baremaps',
    'source-layer': 'leisure',
    layout: {
        visibility: 'visible',
    },
    directives: directives.map(withFillSortKey),
}
