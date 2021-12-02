---
layout: default
title: NaturalEarth Example
---

# NaturalEarth Example

[Natural Earth](https://www.naturalearthdata.com/) is a public domain map dataset available at 1:10m, 1:50m, and 1:110 million scales.
This example shows how to create vector tiles from the Natural Earth dataset.

The first step consists in downloading and decompressing the Natural Earth data. Notice that you must be located in the example's folder which contains the files (for example baremaps/docs/examples/naturalearth).:

```bash
wget https://naturalearth.s3.amazonaws.com/packages/natural_earth_vector.sqlite.zip
unzip -d data natural_earth_vector.sqlite.zip
```

Then, install `ogr2ogr` in order to import the data in postgresql:

```bash
sudo apt-get install gdal-bin
```
or

```bash
brew install gdal
```

You can then import the data using the following command. Here, notice the re-projection in web mercator (EPSG:3857).
The Natural Earth tables should become visible in your postgresql database. Baremaps container which is inside Docker must be running (c.f. [installation](https://www.baremaps.com/installation/)).

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

In order to run the next commands, you have to install Postgres, for example [here](https://gist.github.com/ibraheem4/ce5ccd3e4d7a65589ce84f2a3b7c23a3).

To improve performance, a spatial index should be created for each geometry columns.
Such queries can be generated from the schema itself with the following query:

```
psql -U baremaps -h localhost baremaps
```

```postgresql
SELECT concat('CREATE INDEX IF NOT EXISTS ', tablename, '_gix ON ', tablename, ' USING SPGIST(geometry);');
FROM pg_tables;
WHERE schemaname = 'public' AND tablename LIKE 'ne_%';
ORDER BY tablename;
```

```
\q
```

These queries have been saved in the `indexes.sql` file. You can execute it with the following command:

```bash
psql -h localhost -U baremaps baremaps < indexes.sql
```

To preview and edit the map in the browser, run the tile server with the following command (you must be in the location of the folder containing the files):

```
baremaps edit \
  --database 'jdbc:postgresql://localhost:5432/baremaps?user=baremaps&password=baremaps' \
  --tileset 'tileset.json' \
  --style 'style.json'
```

Now you can open it ([http://localhost:9000/](http://localhost:9000/)).

## Working with shapefiles

The NaturalEarth dataset is also distributed in the [shapefile format](https://www.naturalearthdata.com/http//www.naturalearthdata.com/download/10m/cultural/ne_10m_admin_0_countries.zip).
As demonstrated in the following command, shapefiles can easily be imported in postgis with `ogr2ogr`.
Here, notice that the data is reprojected in WebMercator (EPSG:3857) to improve performance at query time.

```
ogr2ogr \
  -f "PostgreSQL" "PG:host=localhost user=baremaps dbname=baremaps password=baremaps" \
  "ne_10m_admin_0_countries.shp" \
  -lco GEOMETRY_NAME=geom \
  -lco FID=gid \
  -lco PRECISION=no \
  -nlt PROMOTE_TO_MULTI \
  -nln ne_10m_admin_0_countries \
  -s_srs EPSG:4326 \
  -t_srs EPSG:3857 \
  --config OGR_ENABLE_PARTIAL_REPROJECTION TRUE \
  -overwrite
```
