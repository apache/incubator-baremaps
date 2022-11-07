export default {
    "id": "natural",
    "queries": [
        {
            "minzoom": 3,
            "maxzoom": 6,
            "sql": "SELECT id, tags, geom FROM osm_natural_z$zoom WHERE tags ->> 'natural' IN ('wood', 'scrub', 'heath', 'grassland', 'bare_rock', 'scree', 'shingle', 'sand', 'mud', 'water', 'wetland', 'glacier', 'beach')"
        },
        {
            "minzoom": 6,
            "maxzoom": 12,
            "sql": "SELECT id, tags, geom FROM osm_natural_z$zoom WHERE tags ->> 'natural' IN ('wood', 'scrub', 'heath', 'grassland', 'bare_rock', 'scree', 'shingle', 'sand', 'mud', 'water', 'wetland', 'glacier', 'beach')"
        },
        {
            "minzoom": 12,
            "maxzoom": 20,
            "sql": "SELECT id, tags, geom FROM osm_way_z$zoom WHERE tags ? 'natural' AND tags ->> 'natural' NOT IN ('coastline')"
        },
        {
            "minzoom": 12,
            "maxzoom": 20,
            "sql": "SELECT id, tags, geom FROM osm_relation_z$zoom WHERE tags ? 'natural' AND tags ->> 'natural' NOT IN ('coastline')"
        }
    ]
}
