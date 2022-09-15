export default {
    id: 'tunnel_line',
    source: 'baremaps',
    'source-layer': 'highway',
    type: 'line',
    layout: {
        visibility: 'visible',
        'line-cap': 'square',
        'line-join': 'miter',
    },
    filter: [
        'any',
        ['==', ['get', 'tunnel'], 'yes'],
        ['==', ['get', 'layer'], '-1'],
        ['==', ['get', 'covered'], 'yes'],
    ],
    directives: [
        {
            filter: [
                'any',
                ['==', ['get', 'highway'], 'motorway'],
                ['==', ['get', 'highway'], 'motorway_link'],
            ],
            'line-color': 'rgba(241, 188, 198, 1)',
            'road-width': 12,
            'line-sort-key': 10,
        },
        {
            filter: [
                'any',
                ['==', ['get', 'highway'], 'trunk'],
                ['==', ['get', 'highway'], 'trunk_link'],
            ],
            'line-color': 'rgba(252, 215, 204, 1)',
            'road-width': 8,
            'line-sort-key': 9,
        },
        {
            filter: [
                'any',
                ['==', ['get', 'highway'], 'primary'],
                ['==', ['get', 'highway'], 'primary_link'],
            ],
            'line-color': 'rgba(254, 237, 213, 1)',
            'road-width': 10,
            'line-sort-key': 8,
        },
        {
            filter: [
                'any',
                ['==', ['get', 'highway'], 'secondary'],
                ['==', ['get', 'highway'], 'secondary_link'],
            ],
            'line-color': 'rgba(249, 253, 215, 1)',
            'road-width': 8,
            'line-sort-key': 7,
        },
        {
            filter: [
                'any',
                ['==', ['get', 'highway'], 'tertiary'],
                ['==', ['get', 'highway'], 'tertiary_link'],
            ],
            'line-color': 'rgba(255, 255, 255, 1)',
            'road-width': 8,
            'line-sort-key': 6,
        },
        {
            filter: ['==', ['get', 'highway'], 'unclassified'],
            'line-color': 'rgba(242, 242, 242, 1)',
            'road-width': 4,
            'line-sort-key': 5,
        },
        {
            filter: ['==', ['get', 'highway'], 'residential'],
            'line-color': 'rgba(211, 207, 206, 1)',
            'road-width': 4,
            'line-sort-key': 4,
        },
        {
            filter: ['==', ['get', 'highway'], 'living_street'],
            'line-color': 'rgba(245, 245, 245, 1)',
            'road-width': 4,
            'line-sort-key': 3,
        },
        {
            filter: ['==', ['get', 'highway'], 'service'],
            'line-color': 'rgba(242, 242, 242, 1)',
            'road-width': 4,
            'line-sort-key': 2,
        },
        {
            filter: [
                'all',
                ['==', ['get', 'highway'], 'pedestrian'],
                ['!=', ['get', '$type'], 'Polygon'],
            ],
            'line-color': 'rgba(221, 221, 232, 1)',
            'road-width': 2,
            'line-sort-key': 1,
        },
        {
            filter: ['==', ['get', 'highway'], 'raceway'],
            'line-color': 'rgba(255, 192, 203, 1)',
            'road-width': 2,
            'line-sort-key': 0,
        },
        {
            filter: ['==', ['get', 'highway'], 'track'],
            'line-color': 'rgb(177, 140, 63)',
            'road-width': 2,
            'line-sort-key': 0,
        },
    ],
}
