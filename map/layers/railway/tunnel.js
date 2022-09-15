import line from './line.js'

export default {
    'id': 'railway_tunnel',
    'source': 'baremaps',
    'source-layer': 'railway',
    'type': 'line',
    'filter': ['==', ['get', 'tunnel'], 'yes'],
    'layout': {
        'visibility': 'visible',
        'line-cap': 'round',
        'line-join': 'round',
    },
    'paint': {
        'line-dasharray': [1,2]
    },
    'directives': line.directives
}
