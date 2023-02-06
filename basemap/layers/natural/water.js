export default {
    id: 'natural_water',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'natural',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': true,
    },
    directives: [
        {
            filter: ['==', ['get', 'natural'], 'water'],
            'fill-color': 'rgb(170, 211, 223)',
            'fill-sort-key': 10,
        }
    ]
}
