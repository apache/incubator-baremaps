export default {
    "id": "route",
    "queries": [
        {
            "minzoom": 9,
            "maxzoom": 12,
            "sql": "SELECT id, tags, geom FROM osm_ways_z$zoom WHERE tags ? 'route'"
        },
        {
            "minzoom": 12,
            "maxzoom": 20,
            "sql": "SELECT id, tags, geom FROM osm_ways_z$zoom WHERE tags ? 'route'"
        }
    ]
}
