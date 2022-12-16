export default {
    id: 'highway',
    queries: [
        {
            minzoom: 4,
            maxzoom: 6,
            sql:
                "SELECT id, tags, geom FROM osm_highway_z$zoom WHERE tags ->> 'highway' IN ( 'motorway')",
        },
        {
            minzoom: 6,
            maxzoom: 9,
            sql:
                "SELECT id, tags, geom FROM osm_highway_z$zoom WHERE tags ->> 'highway' IN ( 'motorway',  'trunk',  'primary')",
        },
        {
            minzoom: 9,
            maxzoom: 10,
            sql:
                "SELECT id, tags, geom FROM osm_highway_z$zoom WHERE tags ->> 'highway' IN ( 'motorway', 'motorway_link',  'trunk', 'trunk_link',  'primary', 'primary_link',  'secondary', 'secondary_link')",
        },
        {
            minzoom: 10,
            maxzoom: 11,
            sql:
                "SELECT id, tags, geom FROM osm_highway_z$zoom WHERE tags ->> 'highway' IN ( 'motorway', 'motorway_link',  'trunk', 'trunk_link',  'primary', 'primary_link',  'secondary', 'secondary_link',  'tertiary', 'tertiary_link')",
        },
        {
            minzoom: 11,
            maxzoom: 14,
            sql:
                "SELECT id, tags, geom FROM osm_highway_z$zoom WHERE tags ->> 'highway' IN ( 'motorway', 'motorway_link',  'trunk', 'trunk_link',  'primary', 'primary_link',  'secondary', 'secondary_link',  'tertiary', 'tertiary_link', 'unclassified', 'residential')",
        },
        {
            minzoom: 14,
            maxzoom: 20,
            sql:
                "SELECT id, tags, geom FROM osm_ways_z$zoom WHERE tags ? 'highway'",
        },
    ],
}
