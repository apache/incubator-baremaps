export default {
    id: 'natural_trunk',
    type: 'circle',
    filter: ['all', ['==', 'natural', 'tree']],
    source: 'baremaps',
    'source-layer': 'point',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'circle-color': 'rgb(129, 94, 39)',
        'circle-radius': [
            'interpolate',
            ['exponential', 2],
            ['zoom'],
            14,
            0,
            22,
            50,
        ],
    },
};
