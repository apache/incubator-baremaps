export default {
    "id": "railway",
    "queries": [
        {
            "minzoom": 9,
            "maxzoom": 12,
            "sql": "SELECT id, tags, geom FROM osm_railway_z$zoom"
        },
        {
            "minzoom": 12,
            "maxzoom": 20,
            "sql": "SELECT id, tags, geom FROM osm_ways_z$zoom WHERE tags ? 'railway'"
        }
    ]
}
