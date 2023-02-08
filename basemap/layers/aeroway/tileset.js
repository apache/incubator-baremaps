export default {
    id: 'aeroway',
    queries: [
        {
            minzoom: 12,
            maxzoom: 20,
            sql:
                "SELECT id, tags, geom FROM osm_ways_z$zoom WHERE tags ? 'aeroway'",
        },
    ],
}
