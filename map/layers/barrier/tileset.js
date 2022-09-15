export default {
    id: 'barrier',
    queries: [
        {
            minzoom: 14,
            maxzoom: 20,
            sql:
                "SELECT id, tags, geom FROM osm_way_z$zoom WHERE tags ? 'barrier'",
        },
    ],
}
