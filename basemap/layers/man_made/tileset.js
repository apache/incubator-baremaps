export default {
    "id": "man_made",
    "queries": [
        {
            "minzoom": 14,
            "maxzoom": 20,
            "sql": "SELECT id, tags, geom FROM osm_ways_z$zoom WHERE tags ? 'man_made'"
        }
    ]
}
