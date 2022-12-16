export default [
    {
        id: 'water_slide_casing',
        type: 'line',
        filter: [
            'all',
            ['==', ['get', 'attraction'], 'water_slide'],
            ['>=', ['zoom'], 15],
        ],
        source: 'baremaps',
        'source-layer': 'attraction',
        paint: {
            'line-width': ['interpolate', ['exponential', 1], ['zoom'], 16, 2, 20, 8],
        },
    },
    {
        id: 'water_slide',
        type: 'line',
        filter: [
            'all',
            ['==', ['get', 'attraction'], 'water_slide'],
            ['>=', ['zoom'], 16],
        ],
        source: 'baremaps',
        'source-layer': 'attraction',
        paint: {
            'line-color': 'rgba(170, 224, 203, 1)',
            'line-width': [
                'interpolate',
                ['exponential', 1],
                ['zoom'],
                15,
                0.5,
                16,
                1,
                20,
                6,
            ],
        },
    },
]
