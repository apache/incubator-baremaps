export default {
    "id": "power",
    "queries": [
        {
            "minzoom": 12,
            "maxzoom": 20,
            "sql": "SELECT id, tags, geom FROM osm_ways_z$zoom WHERE tags ->> 'power' IN ('cable', 'line', 'minor_line', 'plant', 'substation')"
        }
    ]
}
