export default {
    id: 'boundary',
    queries: [
        {
            minzoom: 1,
            maxzoom: 6,
            sql:
                "SELECT fid as id, jsonb_build_object('boundary', 'administrative', 'admin_level', '0') as tags, geom FROM globaladm0_z$zoom",
        },
        {
            minzoom: 6,
            maxzoom: 14,
            sql:
                "SELECT fid as id, jsonb_build_object('boundary', 'administrative', 'admin_level', '1') as tags, geom FROM globaladm1_z$zoom",
        },
        {
            minzoom: 14,
            maxzoom: 20,
            sql:
                "SELECT id, tags, geom FROM osm_ways_z$zoom WHERE tags ? 'boundary'",
        },
    ],
}
