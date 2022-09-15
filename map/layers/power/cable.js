export default {
    id: 'power_cable',
    type: 'line',
    filter: [
        'any',
        ['==', 'power', 'cable'],
        ['==', 'power', 'line'],
        ['==', 'power', 'minor_line'],
    ],
    source: 'baremaps',
    'source-layer': 'power',
    layout: {
        'line-cap': 'round',
        'line-join': 'round',
        visibility: 'visible',
    },
    paint: {
        'line-width': ['interpolate', ['exponential', 1.2], ['zoom'], 4, 0, 20, 4],
        'line-color': 'rgb(171, 171, 171)',
    },
}
