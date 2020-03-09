# OpenStreetMap Example

As a small country, Liechtenstein is suitable for testing and fits well in a git repository. 
You can import the OSM data in postgis using the following command:

```bash
baremaps import \
  --input 'liechtenstein-latest.osm.pbf' \
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