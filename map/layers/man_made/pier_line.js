export default {
    id: 'man_made_pier_line',
    type: 'line',
    filter: ['==', ['get', 'man_made'], 'pier'],
    source: 'baremaps',
    'source-layer': 'man_made',
    layout: {
        visibility: 'visible',
    },
    minzoom: 12,
    paint: {
        'line-color': 'rgb(242, 239, 233)',
        'line-width': ['interpolate', ['exponential', 1], ['zoom'], 12, 0.5, 18, 2],
    },
}
