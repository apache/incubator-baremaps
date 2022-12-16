export default {
    "id": "tourism",
    "queries": [
        {
            "minzoom": 14,
            "maxzoom": 20,
            "sql": "SELECT id, tags, geom FROM osm_relations_z$zoom WHERE tags ? 'tourism'"
        }
    ]
}
