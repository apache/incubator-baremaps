export default {
    id: 'label',
    type: 'symbol',
    source: 'baremaps',
    'source-layer': 'point',
    layout: {
        visibility: 'visible',
        'text-font': ['Noto Sans Regular'],
        'text-field': ['get', 'name'],
    },
    paint: {
        'text-halo-color': 'rgba(255, 255, 255, 0.8)',
        'text-halo-width': 1,
    },
    directives: [
        {
            filter: ['==', ['get', 'place'], 'city'],
            'text-size': 16,
            'text-color': 'rgb(25, 25, 25)',
        },
        {
            filter: ['==', ['get', 'place'], 'town'],
            'text-size': 14,
            'text-color': 'rgb(50,50,50)',
        },
        {
            filter: ['==', ['get', 'place'], 'village'],
            'text-size': 12,
            'text-color': 'rgb(75,75,75)',
        },
        {
            filter: ['==', ['get', 'place'], 'locality'],
            'text-size': 12,
            'text-color': 'rgb(75,75,75)',
        },
        // {
        //     filter: [
        //         'in',
        //         ['get', 'place'],
        //         [
        //             'literal', [
        //                 'neighbourhood',
        //                 'quarter',
        //                 'hamlet',
        //                 'isolated_dwelling',
        //                 'islet'
        //             ]
        //         ]
        //     ],
        //     'text-size': 11,
        //     'text-color': 'rgba(100, 100, 100, 1)',
        // },

    ]
}
