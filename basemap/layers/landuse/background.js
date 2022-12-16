export default {
    id: 'landuse_background',
    type: 'fill',
    source: 'baremaps',
    'source-layer': 'landuse',
    layout: {
        visibility: 'visible',
    },
    directives: [
        {
            filter: ['==', ['get', 'landuse'], 'village_green'],
            'fill-color': 'rgb(205, 235, 176)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'salt_pond'],
            'fill-color': 'rgb(170, 211, 223)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'religious'],
            'fill-color': 'rgb(205, 204, 201)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'recreation_ground'],
            'fill-color': 'rgb(223, 252, 226)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'railway'],
            'fill-color': 'rgb(236, 218, 233)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'quarry'],
            'fill-color': 'rgb(195, 194, 194)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'plant_nursery'],
            'fill-color': 'rgb(174, 223, 162)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'military'],
            'fill-color': 'rgb(242, 228, 221)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'landfill'],
            'fill-color': 'rgb(182, 182, 144)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'greenfield'],
            'fill-color': 'rgb(242, 238, 232)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'garages'],
            'fill-color': 'rgb(222, 221, 204)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'cemetery'],
            'fill-color': 'rgb(170, 203, 175)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'brownfield'],
            'fill-color': 'rgb(182, 182, 144)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'basin'],
            'fill-color': 'rgb(170, 211, 223)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'vineyard'],
            'fill-color': 'rgb(172, 225, 161)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'meadow'],
            'fill-color': 'rgb(205, 235, 176)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'farmyard'],
            'fill-color': 'rgb(238, 213, 179)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'farmland'],
            'fill-color': 'rgb(237, 240, 214)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'allotments'],
            'fill-color': 'rgb(202, 224, 191)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'retail'],
            'fill-color': 'rgb(254, 213, 208)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'industrial'],
            'fill-color': 'rgb(235, 219, 232)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'residential'],
            'fill-color': 'rgb(225, 225, 225)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'construction'],
            'fill-color': 'rgb(199, 199, 180)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'commercial'],
            'fill-color': 'rgb(242, 216, 217)',
        },
        {
            filter: ['==', ['get', 'landuse'], 'pedestrian'],
            'fill-color': 'rgb(221, 221, 233)',
        },
    ],


    paint: {
        'fill-antialias': true,
    },
}
