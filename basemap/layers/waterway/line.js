export default {
    "id": "waterway",
    "type": "line",
    "filter": [
        "all",
        ["!=", "tunnel", "yes"],
        ["!=", "tunnel", "culvert"]
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
            4, 0, 20, 12
        ],
        "line-color": "rgb(170, 211, 223)"
    }
}