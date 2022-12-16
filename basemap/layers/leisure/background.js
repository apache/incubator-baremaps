export default {
    id: 'leisure',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'leisure',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': true,
    },
    directives: [
        {
            filter: ['==', ['get', 'leisure'], 'swimming_pool'],
            'fill-color': 'rgb(170, 211, 223)',
            'fill-outline-color': 'rgb(120, 183, 202)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'sports_centre'],
            'fill-color': 'rgb(223, 252, 226)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'miniature_golf'],
            'fill-color': 'rgb(181, 226, 181)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'ice_rink'],
            'fill-color': 'rgb(221, 236, 236)',
            'fill-outline-color': 'rgb(140, 220, 189)',
            'fill-sort-key': 10,
        },
        {
            filter: ['==', ['get', 'leisure'], 'golf_course'],
            'fill-color': 'rgb(181, 226, 181)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'garden'],
            'fill-color': 'rgb(205, 235, 176)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'dog_park'],
            'fill-color': 'rgb(224, 252, 227)',
            'fill-sort-key': 10,
        },
        {
            filter: ['==', ['get', 'leisure'], 'playground'],
            'fill-color': 'rgb(223, 252, 226)',
            'fill-outline-color': 'rgb(164, 221, 169)',
            'fill-sort-key': 20,
        },
        {
            filter: ['==', ['get', 'leisure'], 'track'],
            'fill-color': 'rgb(196, 224, 203)',
            'fill-sort-key': 10,
        },
        {
            filter: ['==', ['get', 'leisure'], 'stadium'],
            'fill-color': 'rgb(223, 252, 226)',
        },
        {
            filter: ['==', ['get', 'leisure'], 'pitch'],
            'fill-color': 'rgb(170, 224, 203)',
            'fill-outline-color': 'rgb(151, 212, 186)',
            'fill-sort-key': 10,
        },
        {
            filter: ['==', ['get', 'leisure'], 'swimming_pool'],
            'fill-color': 'rgb(170, 211, 223)',
            'fill-outline-color': 'rgb(120, 183, 202)',
            'fill-sort-key': 10,
        },
        {
            filter: ['==', ['get', 'leisure'], 'park'],
            'fill-color': 'rgb(200, 250, 204)',
            'fill-sort-key': 10,
        },
    ],
}
