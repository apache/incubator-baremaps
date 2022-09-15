export default {
    id: 'boundary',
    type: 'line',
    source: 'baremaps',
    'source-layer': 'boundary',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'line-dasharray': [4, 1, 1, 1],
    },
    directives: [
        {
            filter: ['==', ['get', 'admin_level'], "1"],
            'line-color': 'rgb(207, 155, 203)',
        },
        {
            filter: ['==', ['get', 'admin_level'], "2"],
            'line-color': 'rgb(207, 155, 203)',
        },
        {
            filter: ['==', ['get', 'admin_level'], "3"],
            'line-color': 'rgb(207, 155, 203)',
        },
        {
            filter: ['==', ['get', 'admin_level'], "4"],
            'line-color': 'rgb(207, 155, 203)',
        },
    ],

}
