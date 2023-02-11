import {withLineSortKey} from "../../utils/utils.js";

let directive = [
    {
        filter: ['==', ['get', 'route'], 'ferry'],
        'line-color': 'rgb(112, 181, 201)',
        'line-width': 1
    },
];

export default {
    id: 'route_ferry',
    type: 'line',
    source: 'baremaps',
    'source-layer': 'route',
    layout: {
        'line-cap': 'round',
        'line-join': 'round',
        visibility: 'visible',
    },
    directives: directive.map(withLineSortKey),
}
