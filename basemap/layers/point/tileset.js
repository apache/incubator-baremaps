export default {
    "id": "point",
    "queries": [
        {
            "minzoom": 1,
            "maxzoom": 20,
            "sql": "SELECT id, tags, geom FROM osm_nodes_z$zoom WHERE tags != '{}'"
        }
    ]
}
