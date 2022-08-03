---
layout: default
title: Contour Lines in Vector Tiles
---

# Contour Lines in Vector Tiles

This example demonstrates how to produce contours from a digital elevation model (DEM) and how to display them with vector tiles.

The approach consists in using the [`gdal_contour`](https://gdal.org/programs/gdal_contour.html) command.
Therefore, start by installing `gdal`:

```bash
sudo apt-get install gdal-bin
```

The geotiff present in this directory comes from the [ASTER](https://asterweb.jpl.nasa.gov/gdem.asp) dataset.
We used the following command to reproject the geotiff in the desired projection (e.g. WebMercator) before importing it in the database.

```
gdalwarp -rc \
  -s_srs epsg:4326 -t_srs epsg:3857 \
  -dstnodata 0 -of GTiff -co tiled=yes \
  liecthenstein-aster-dem-v2.tif \
  liecthenstein-aster-dem-v2-3857.tif
```

You can now import any GeoTiff DEM as contours in postgis. 
In the following command, the `-nln` argument name the table that contains the data, 
the `-a` argument name the column that contains the elevation, 
the `-i` argument specifies the interval in meters at which contours are generated. 

```
gdal_contour \
  -a elevation -nln aster_dem -i 10 \
  -f PostgreSQL \
  liecthenstein-aster-dem-v2-3857.tif "PG:host=localhost user=baremaps password=baremaps dbname=baremaps"
```

Some index can now be added to the database to improve performances. 
When available, a smoothing function such as `ST_ChaikinSmoothing` can be used to improve rendering of the contours. 

```postgresql
DROP INDEX IF EXISTS aster_dem_gix;
CREATE INDEX CONCURRENTLY IF NOT EXISTS aster_dem_gix ON aster_dem USING SPGIST(wkb_geometry);
```

Finally, to preview the map in the browser, run the tile server with the following command. 
Your changes to the configuration files will be automatically reloaded by the browser.

```bash
baremaps map dev \
  --database 'jdbc:postgresql://localhost:5432/baremaps?user=baremaps&password=baremaps' \
  --tileset 'tileset.json' \
  --style 'style.json'
```
