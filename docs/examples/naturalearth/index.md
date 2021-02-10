---
layout: default
title: NaturalEarth Example
---

# NaturalEarth Example

[Natural Earth](https://www.naturalearthdata.com/) is a public domain map dataset available at 1:10m, 1:50m, and 1:110 million scales.
This example shows how to create vector tiles from the Natural Earth dataset.

The first step consists in downloading and decompressing the Natural Earth data:

```bash
wget http://naciscdn.org/naturalearth/packages/natural_earth_vector.sqlite.zip
unzip -d data natural_earth_vector.sqlite.zip
```

Then, install `ogr2ogr` in order to import the data in postgresql:

```bash
sudo apt-get install gdal-bin
```

You can then import the data using the following command. Here, notice the re-projection in web mercator (EPSG:3857).
The Natural Earth tables should become visible in your postgresql database. 

```bash
PGCLIENTENCODING=UTF8 ogr2ogr \
    -progress \
    -f Postgresql \
    -s_srs EPSG:4326 \
    -t_srs EPSG:3857 \
    -clipsrc -180.1 -85.0511 180.1 85.0511 \
    PG:"dbname=baremaps user=baremaps host=localhost password=baremaps port=5432" \
    -lco GEOMETRY_NAME=geometry \
    -lco OVERWRITE=YES \
    -lco DIM=2 \
    -nlt GEOMETRY \
    -overwrite \
    "data/packages/natural_earth_vector.sqlite"
```

To improve performance, an spatial index should be created for each geometry columns. 
Such queries can be generated from the schema itself with the following query:

```postgresql
SELECT concat('CREATE INDEX IF NOT EXISTS ', tablename, '_gix ON ', tablename, ' USING SPGIST(geometry);')
FROM pg_tables
WHERE schemaname = 'public' AND tablename LIKE 'ne_%'
ORDER BY tablename
```

These queries have been saved in the `indexes.sql` file. You can execute it with the following command:

```bash
psql -h localhost -U baremaps baremaps < indexes.sql
```

To preview the data, run the tile server with the following command:

```
baremaps serve \
  --database 'jdbc:postgresql://localhost:5432/baremaps?allowMultiQueries=true&user=baremaps&password=baremaps' \
  --source 'source.yaml'
```
