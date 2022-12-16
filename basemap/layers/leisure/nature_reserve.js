export default {
    id: 'leisure_nature_reserve',
    type: 'line',
    source: 'baremaps',
    'source-layer': 'leisure',
    layout: {
        visibility: 'visible',
    },
    directives: [
        {
            filter: ['==', 'leisure', 'nature_reserve'],
            'line-width': 5,
            'line-color': 'rgba(230, 233, 222, 0.5)',
        },
    ],
}
