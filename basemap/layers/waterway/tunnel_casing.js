export default {
    "id": "waterway_tunnel_casing",
    "type": "line",
    "filter": [
        "any",
        ["==", "tunnel", "yes"],
        ["==", "tunnel", "culvert"]
    ],
    "source": "baremaps",
    "source-layer": "waterway",
    "layout": {
        "visibility": "visible"
    },
    "paint": {
        "line-width": [
            "interpolate", ["exponential", 1.2], ["zoom"], 4, 0, 20,
            12
        ],
        "line-color": "rgb(170, 211, 223)",
        "line-dasharray": [0.3, 0.15]
    }
}