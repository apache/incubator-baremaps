# OpenStreetMap Example

As a small country, Liechtenstein is suitable for testing and fits well in a git repository. 
You can import the OSM data in postgis using the following command:

```bash
gazetteer import \
  'examples/openstreetmap/liechtenstein-latest.osm.pbf' \
  'jdbc:postgresql://localhost:5432/gazetteer?allowMultiQueries=true&user=gazetteer&password=gazetteer'
```

To preview the data, start the tile server:

```bash
gazetteer serve \
  'jdbc:postgresql://localhost:5432/gazetteer?allowMultiQueries=true&user=gazetteer&password=gazetteer' \
  'examples/openstreetmap/config.yaml' \
  'examples/openstreetmap/static/' \
  --tile-reader with
```

Well done, the tile server should have started and a map of liechtenstein should appear in your browser ([http://localhost:9000/](http://localhost:8082/))!

Vector tiles are rarely served dynamically in production. The following command produces a directory that contains precomputed tiles that can be deployed on a CDN:

```bash
gazetteer tiles \
  'examples/openstreetmap/config.yaml' \
  'jdbc:postgresql://localhost:5432/gazetteer?allowMultiQueries=true&user=gazetteer&password=gazetteer' \
  'examples/openstreetmap/tiles/' \
  --minZoom 14 \
  --maxZoom 14
```