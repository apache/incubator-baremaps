export default {
    id: 'amenity_fill_2',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'amenity',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': true,
    },
    directives: [
        {
            filter: ['==', ['get', 'amenity'], 'fountain'],
            'fill-color': 'rgb(170, 211, 223)',
            'fill-outline-color': 'rgb(170, 211, 223)',
        },
    ],
}
