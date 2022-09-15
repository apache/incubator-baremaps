export default [
    {
        id: 'barrier_guard_rail',
        type: 'line',
        filter: ['all', ['==', 'barrier', 'guard_rail']],
        source: 'baremaps',
        'source-layer': 'barrier',
        paint: {
            'line-color': 'rgba(139, 177, 162, 1)',
        },
    },
]
