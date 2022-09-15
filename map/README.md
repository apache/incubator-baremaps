# OpenStreetMap Vecto

🚧 🚧 Work in progress 🚧 🚧

OpenStreetMap Vecto is a general-purpose configuration for [Baremaps](https://github.com/baremaps/baremaps).
It is used to generate vector tiles and to produce a Mapbox style inspired by [OpenStreetMap Carto](https://github.com/gravitystorm/openstreetmap-carto).

## Requirements

* [Postgres](https://www.postgresql.org/) 13+
* [PostGIS](https://postgis.net/) 3+
* [Java](https://adoptium.net/) 17+
* [Baremaps](https://www.baremaps.com/) 0.7+

The database should be accessible with the following jdbc settings:

```
jdbc:postgresql://localhost:5432/baremaps?user=baremaps&password=baremaps
```

## Getting started

In order to experiment with OpenStreetMap Vecto, you first need to clone the current repository
 
```
git clone git@github.com:baremaps/openstreetmap-vecto.git
cd openstreetmap-vecto/
```

Assuming that the necessary requirements have been installed, the database can be populated with the following command.

```
baremaps workflow execute --file workflow.json
```

The development server can be started with the following command.

```
baremaps map dev --log-level DEBUG \
  --database 'jdbc:postgresql://localhost:5432/baremaps?user=baremaps&password=baremaps' \
  --tileset 'tileset.json' \
  --style 'style.json'
```

## Editing the tileset

The configuration format used in the `tileset.json` file extends the [TileJSON specification](https://github.com/mapbox/tilejson-spec/tree/master/2.2.0).
Simply put, it adds in the ability to describe the `vector_tiles` and their content with SQL queries that follow the Postgis dialect.

```
{
  "tilejson": "2.2.0",
  "tiles": [
    "http://localhost:9000/tiles/{z}/{x}/{y}.mvt"
  ],
  "vector_layers": [
    {
      "id": "aerialway",
      "queries": [
        {
          "minzoom": 14,
          "maxzoom": 20,
          "sql": "SELECT id, tags, geom FROM osm_ways_z${zoom} WHERE tags ? 'aerialway'"
        }
      ]
    }
  ]
}
```

## Editing the style

The configuration format used in the `style.json` follows the [Mapbox style specification](https://github.com/mapbox/mapbox-gl-js).
Baremaps integrates [Maputnik](https://maputnik.github.io/) and most of the modifications will take place in the browser.

## Tools

* [Overpass turbo](https://overpass-turbo.eu/) from [taginfo](https://taginfo.openstreetmap.org/)

## Contributing

As a lot remains to be done, contributions and feedbacks are welcome.
