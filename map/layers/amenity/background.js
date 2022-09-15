export default {
    id: 'amenity_fill_1',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'amenity',
    layout: {
        visibility: 'visible',
    },
    paint: {
        'fill-antialias': true,
    },
    directives: [
        {
            filter: ['==', ['get', 'amenity'], 'kindergarten'],
            'fill-color': 'rgb(255, 255, 228)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'school'],
            'fill-color': 'rgb(255, 255, 228)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'college'],
            'fill-color': 'rgb(255, 255, 228)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'university'],
            'fill-color': 'rgb(255, 255, 228)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'hospital'],
            'fill-color': 'rgb(255, 255, 228)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'grave_yard'],
            'fill-color': 'rgb(170, 203, 175)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'parking'],
            'fill-color': 'rgb(238, 238, 238)',
        },
        {
            filter: ['==', ['get', 'amenity'], 'motorcycle_parking'],
            'fill-color': 'rgb(238, 238, 238)',
        },
    ],
}
