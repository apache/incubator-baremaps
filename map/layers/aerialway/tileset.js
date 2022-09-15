export default {
    id: 'aerialway',
    queries: [
        {
            minzoom: 13,
            maxzoom: 20,
            sql:
                "SELECT id, tags, geom FROM osm_way_z$zoom WHERE tags ? 'aerialway'",
        },
    ],
}
