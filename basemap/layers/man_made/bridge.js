export default {
    id: 'man_made_bridge',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'man_made',
    layout: {
        visibility: 'visible',
    },
    directives: [
        {
            filter: ['==', ['get', 'man_made'], 'bridge'],
            'fill-color': 'rgb(184, 184, 184)',
        },
    ],
    paint: {
        'fill-antialias': true,
    },
}
