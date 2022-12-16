export default {
    id: 'pedestrian_area',
    source: 'baremaps',
    'source-layer': 'highway',
    type: 'fill',
    layout: {
        visibility: 'visible',
    },
    filter: [
        'all',
        ['==', 'area', 'yes'],
        [
            'any',
            ['==', 'highway', 'pedestrian'],
            ['==', 'highway', 'path'],
            ['==', 'highway', 'sidewalk'],
            ['==', 'highway', 'crossing'],
            ['==', 'highway', 'steps'],
            ['==', 'highway', 'footway'],
        ],
    ],
    paint: {
        'fill-color': 'rgb(221, 221, 231)',
        'fill-antialias': true,
    },
}
