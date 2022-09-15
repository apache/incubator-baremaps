export default {
    id: 'highway_label',
    type: 'symbol',
    source: 'baremaps',
    'source-layer': 'highway',
    layout: {
        'symbol-placement': 'line',
        'text-anchor': 'center',
        'text-field': ['get', 'name'],
        'text-font': ['Noto Sans Regular'],
        'text-size': ['interpolate', ['exponential', 1], ['zoom'], 13, 10, 14, 12],
        visibility: 'visible',
    },
    paint: {
        'text-color': 'rgb(96,96,96)',
        'text-halo-color': 'rgba(255,255,255,0.8)',
        'text-halo-width': 1.2,
    },
}
