export default {
    id: 'man_made_bridge',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'man_made',
    layout: {
        visibility: 'visible',
    },
    filter: ['==', ['geometry-type'], 'Polygon'],
    directives: [
        {
            filter: ['in', ['get', 'man_made'], ['literal', ['bridge', 'breakwater']]],
            'fill-color': 'rgb(184, 184, 184)',
        },
    ],
    paint: {
        'fill-antialias': true,
    },
}
