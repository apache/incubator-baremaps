import {asLayerObject, withSortKeys} from "../../utils/utils.js";

let directives = [
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'motorway'],
            ['==', ['get', 'highway'], 'motorway_link'],
        ],
        'line-color': 'rgb(227, 113, 134)',
        'road-width': 12,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'trunk'],
            ['==', ['get', 'highway'], 'trunk_link'],
        ],
        'line-color': 'rgb(248, 163, 132)',
        'road-width': 8,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'primary'],
            ['==', ['get', 'highway'], 'primary_link'],
        ],
        'line-color': 'rgb(252, 202, 137)',
        'road-width': 10,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'secondary'],
            ['==', ['get', 'highway'], 'secondary_link'],
        ],
        'line-color': 'rgb(243, 246, 161)',
        'road-width': 8,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'tertiary'],
            ['==', ['get', 'highway'], 'tertiary_link'],
        ],
        'line-color': 'rgb(229, 229, 229)',
        'road-width': 8,
    },
    {
        filter: ['==', ['get', 'highway'], 'unclassified'],
        'line-color': 'rgb(229, 229, 229)',
        'road-width': 4,
    },
    {
        filter: ['==', ['get', 'highway'], 'residential'],
        'line-color': 'rgb(229, 229, 229)',
        'road-width': 4,
    },
    {
        filter: ['==', ['get', 'highway'], 'living_street'],
        'line-color': 'rgb(213, 213, 213)',
        'road-width': 4,
    },
    {
        filter: ['==', ['get', 'highway'], 'service'],
        'line-color': 'rgb(229, 229, 229)',
        'road-width': 4,
    },
    {
        filter: ['==', ['get', 'highway'], 'track'],
        'line-color': 'rgb(159, 126, 57)',
        'road-width': 2,
    },
    {
        filter: ['==', ['get', 'highway'], 'raceway'],
        'line-color': 'rgb(255, 147, 166)',
        'road-width': 4,
    },
    {
        filter: [
            'all',
            ['==', ['get', 'highway'], 'pedestrian'],
            ['!=', ['get', '$type'], 'Polygon'],
        ],
        'line-color': 'rgb(194, 194, 212)',
        'road-width': 2,
    },
]

export default asLayerObject(withSortKeys(directives), {
    id: 'bridge_line',
    source: 'baremaps',
    'source-layer': 'highway',
    type: 'line',
    layout: {
        visibility: 'visible',
        'line-cap': 'butt',
        'line-join': 'miter',
    },
    filter: ['any', ['==', ['get', 'bridge'], 'yes']],
});
