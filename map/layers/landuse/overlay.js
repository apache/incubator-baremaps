export default {
    id: 'landuse_overlay',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'landuse',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': true,
    },
    directives: [
        {
            filter: ['==', ['get', 'landuse'], 'grass'],
            'fill-color': 'rgb(205, 235, 176)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'forest'],
            'fill-color': 'rgb(171, 210, 156)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'greenhouse_horticulture'],
            'fill-color': 'rgb(237, 240, 214)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'orchard'],
            'fill-color': 'rgb(172, 225, 161)',
        },
    ],
}
