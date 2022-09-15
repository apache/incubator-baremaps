export default {
    "id": "waterway_tunnel",
    "type": "line",
    "filter": [
        "any",
        ["==", "tunnel", "yes"],
        ["==", "tunnel", "culvert"]
    ],
    "source": "baremaps",
    "source-layer": "waterway",
    "layout": {
        "line-cap": "round",
        "line-join": "round",
        "visibility": "visible"
    },
    "paint": {
        "line-width": [
            "interpolate",
            ["exponential", 1.2],
            ["zoom"],
            4, 0, 20, 8
        ],
        "line-color": "rgb(243, 247, 247)"
    }
}