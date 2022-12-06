export default {
    id: 'landuse',
    queries: [
        {
            minzoom: 3,
            maxzoom: 6,
            sql:
                "SELECT id, tags, geom FROM osm_landuse_z$zoom WHERE tags ->> 'landuse' IN ('farmland', 'forest', 'meadow')",
        },
        {
            minzoom: 6,
            maxzoom: 12,
            sql:
                "SELECT id, tags, geom FROM osm_landuse_z$zoom WHERE tags ->> 'landuse' IN ('farmland', 'forest', 'meadow', 'residential', 'vineyard')",
        },
        {
            minzoom: 12,
            maxzoom: 20,
            sql: "SELECT id, tags, geom FROM osm_ways_z$zoom WHERE tags ? 'landuse'"
        },
        {
            minzoom: 12,
            maxzoom: 20,
            sql: "SELECT id, tags, geom FROM osm_relations_z$zoom WHERE tags ? 'landuse'"
        }
    ],
}
