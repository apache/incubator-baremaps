export default {
    id: 'boundary',
    queries: [
        {
            minzoom: 1,
            maxzoom: 5,
            sql:
                "SELECT id, tags, geom FROM osm_boundary_z$zoom WHERE tags ->> 'boundary' IN ('administrative') AND tags ->> 'admin_level' IN ('1', '2')",
        },
        {
            minzoom: 5,
            maxzoom: 13,
            sql:
                "SELECT id, tags, geom FROM osm_boundary_z$zoom WHERE tags ->> 'boundary' IN ('administrative') AND tags ->> 'admin_level' IN ('1', '2', '3', '4')",
        },
        {
            minzoom: 13,
            maxzoom: 20,
            sql:
                "SELECT id, tags, geom FROM osm_ways_z$zoom WHERE tags ? 'boundary'",
        },
    ],
}
