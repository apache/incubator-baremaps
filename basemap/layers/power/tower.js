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
        "circle-color": "rgb(171, 171, 171)",
        "circle-radius": [
            "interpolate", ["exponential", 1], ["zoom"], 13, 1,
            14, 2, 15, 3, 16, 4, 17, 5, 18, 6
        ]
    }
}
