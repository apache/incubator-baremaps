# Building Example

This example builds upon the OpenStreetMap example and shows how 3d buildings can be displayed with Mapbox.
First, start by downloading the OSM data for London in the current directory

```
wget https://download.geofabrik.de/europe/great-britain/england/greater-london-latest.osm.pbf
```
You can then import the data in postgis using the following command:

```bash
baremaps import \
  --input 'greater-london-latest.osm.pbf' \
  --database 'jdbc:postgresql://localhost:5432/baremaps?allowMultiQueries=true&user=baremaps&password=baremaps'
```

To preview the data, start the tile server:

```bash
baremaps serve \
  --database 'jdbc:postgresql://localhost:5432/baremaps?allowMultiQueries=true&user=baremaps&password=baremaps' \
  --config 'config.yaml' \
  --assets 'static/' \
  --reader fast
```

Well done, the tile server should have started and a map of liechtenstein should appear in your browser ([http://localhost:9000/](http://localhost:8082/))!

Vector tiles are rarely served dynamically in production. The following command produces a directory that contains precomputed tiles that can be deployed on a CDN:

```bash
baremaps export \
  --database 'jdbc:postgresql://localhost:5432/baremaps?allowMultiQueries=true&user=baremaps&password=baremaps' \
  --config 'config.yaml' \
  --repository 'tiles/' \
  --minZoom 14 \
  --maxZoom 14 \
  --reader fast
```