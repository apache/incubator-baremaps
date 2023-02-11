export default {
    "id": "power_tower",
    "type": "circle",
    "filter": [
        "any",
        ["==", "power", "tower"],
        ["==", "power", "pole"],
        ["==", "power", "portal"],
        ["==", "power", "catenary_mast"]
    ],
    "source": "baremaps",
    "source-layer": "point",
    "layout": {
        "visibility": "visible"
    },
    "paint": {
        'circle-pitch-alignment': 'map',
        "circle-color": "rgb(171, 171, 171)",
        "circle-radius": [
            "interpolate",
            ["exponential", 1],
            ["zoom"],
            14, 1,
            20, 8
        ]
    }
}
