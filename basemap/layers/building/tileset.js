export default {
    id: 'building',
    queries: [
        {
            minzoom: 13,
            maxzoom: 20,
            sql:
                "SELECT id, tags, geom FROM osm_ways_z$zoom WHERE tags ? 'building'",
        },
        {
            minzoom: 13,
            maxzoom: 20,
            sql:
                "SELECT id, tags, geom FROM osm_relations_z$zoom WHERE tags ? 'building'",
        },
    ],
}
