export default {
    id: 'man_made_pier_label',
    type: 'symbol',
    filter: ['all', ['==', 'man_made', 'pier']],
    source: 'baremaps',
    'source-layer': 'man_made',
    layout: {
        'text-field': ['get', 'name'],
        'text-font': ['Noto Sans Regular'],
        'symbol-placement': 'line-center',
        'text-size': [
            'interpolate',
            ['exponential', 1],
            ['zoom'],
            15,
            8,
            16,
            11,
            20,
            11,
        ],
        visibility: 'visible',
    },
    minzoom: 15,
    paint: {
        'text-halo-color': 'rgba(255,255,255,0.8)',
        'text-halo-width': 1.2,
    },
}
