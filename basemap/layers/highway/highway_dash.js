export default {
    id: 'highway_dash',
    type: 'line',
    source: 'baremaps',
    'source-layer': 'highway',
    layout: {
        'line-cap': 'round',
        'line-join': 'round',
        visibility: 'visible',
    },
    paint: {
        'line-dasharray': [2, 2],
    },
    directives: [
        {
            filter: ['==', ['get', 'highway'], 'bridleway'],
            'line-color': 'rgb(68, 159, 66)',
            'road-width': 1,
        },
        {
            filter: [
                'any',
                ['==', ['get', 'highway'], 'cycleway'],
                [
                    'all',
                    ['==', ['get', 'highway'], 'path'],
                    ['==', ['get', 'bicycle'], 'designated'],
                ],
            ],
            'line-color': 'rgba(28, 27, 254, 1)',
            'road-width': 1,
        },
        {
            filter: [
                'any',
                [
                    'all',
                    ['==', ['get', 'highway'], 'footway'],
                    ['==', ['get', 'access'], 'private'],
                ],
                [
                    'all',
                    ['==', ['get', 'highway'], 'service'],
                    ['in', ['get', 'access'], ['literal', ['private', 'no']]],
                ],
            ],
            'line-color': 'rgb(192, 192, 192)',
            'road-width': 1,
        },
        {
            filter: [
                'any',
                [
                    'in',
                    ['get', 'highway'],
                    ['literal', ['sidewalk', 'crossing', 'steps']],
                ],
                [
                    'all',
                    ['==', ['get', 'highway'], 'footway'],
                    ['!=', ['get', 'access'], 'private'],
                ],
                [
                    'all',
                    ['==', ['get', 'highway'], 'path'],
                    ['!=', ['get', 'bicycle'], 'designated'],
                ],
            ],
            'line-color': 'rgb(250, 132, 117)',
            'road-width': 1,
        },
        {
            filter: ['all', ['==', ['get', 'highway'], 'track']],
            'line-color': 'rgb(177, 140, 63)',
            'road-width': 1,
        },
    ],
}
