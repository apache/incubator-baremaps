export default {
    id: 'power_plant',
    type: 'fill',
    filter: ['any', ['==', 'power', 'plant'], ['==', 'power', 'substation']],
    source: 'baremaps',
    'source-layer': 'power',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-color': 'rgb(226, 203, 222)',
        'fill-antialias': true,
        'fill-outline-color': 'rgb(171, 171, 171)',
    },
}
