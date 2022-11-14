---
layout: default
title: NaturalEarth Example
---

# NaturalEarth Example

[Natural Earth](https://www.naturalearthdata.com/) is a public domain map dataset available at 1:10m, 1:50m, and 1:110 million scales.
This example shows how to create vector tiles from the Natural Earth dataset.

The first step consists in downloading the Natural Earth data, decompressing it, and importing it in the database. 
The following workflow will allow you to achieve this result.

```
baremaps workflow execute --file workflow.json
```

To preview and edit the map in the browser, run the tile server with the following command:

```
baremaps map dev \
  --database 'jdbc:postgresql://localhost:5432/baremaps?user=baremaps&password=baremaps' \
  --tileset 'tileset.json' \
  --style 'style.json'
```

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
