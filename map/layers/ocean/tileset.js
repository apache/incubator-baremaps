export default {
    "id": "ocean",
    "queries": [
        {
            "minzoom": 0,
            "maxzoom": 10,
            "sql": "SELECT row_number() OVER () as id, '{\"ocean\":\"water\"}'::jsonb, geometry FROM simplified_water_polygons_shp"
        },
        {
            "minzoom": 10,
            "maxzoom": 20,
            "sql": "SELECT row_number() OVER () as id, '{\"ocean\":\"water\"}'::jsonb, geometry FROM water_polygons_shp"
        }
    ]
}
