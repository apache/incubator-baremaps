export default {
    id: 'leisure',
    queries: [
        {
            minzoom: 13,
            maxzoom: 20,
            sql:
                "SELECT id, tags, geom FROM osm_way_z$zoom WHERE tags ? 'leisure'",
        },
        {
            minzoom: 13,
            maxzoom: 20,
            sql:
                "SELECT id, tags, geom FROM osm_relation_z$zoom WHERE tags ? 'leisure'",
        },
    ],
}
