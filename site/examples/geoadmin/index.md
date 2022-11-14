---
layout: default
title: Geoadmin Noise Exposure in Vector Tiles
---

# Geoadmin noise exposure in vector tiles

The Federal Office for the Environment (FOEN) provides a GeoTiff describing the [traffic noise levels](https://www.bafu.admin.ch/bafu/de/home/zustand/daten/geodaten/laerm--geodaten.html) in Switzerland.
Download an uncompress the archive that contains the GeoTiff.

If needed, you can now reproject the geotiff in the desired projection before importing it in the database.
Start by reprojecting the geotiff (EPSG:2056) from LV95 to WebMercator (EPSG:3857)

```
gdalwarp -rc \
  -s_srs epsg:2056 -t_srs epsg:3857 \
  -dstnodata 0 -of GTiff -co tiled=yes \
  StrassenLaerm_Tag_LV95.tif \
  StrassenLaerm_Tag_LV95-3857.tif
```

You can now import any GeoTiff DEM as contours in postgis.
Here, the `-nln` argument name the table that contains the data,
the `-a` argument name the column that contains the elevation,
the `-i` argument specifies the interval in meters at which contours are generated.

```
gdal_polygonize.py \
  StrassenLaerm_Tag_LV95-3857.tif \
  -f PostgreSQL \
  "PG:host=localhost user=baremaps password=baremaps dbname=baremaps" \
  geoadmin_traffic_noise_day
```

The following index can now be created to improve performances.
When available, a smoothing function such as `ST_ChaikinSmoothing` can be used to improve rendering.

```postgresql
DROP INDEX IF EXISTS geoadmin_traffic_noise_day_gix;
CREATE INDEX CONCURRENTLY IF NOT EXISTS geoadmin_traffic_noise_day_gix ON geoadmin_traffic_noise_day USING SPGIST(wkb_geometry);
```

To preview and edit the map in the browser, run the tile server with the following command:

```bash
baremaps map dev \
  --database 'jdbc:postgresql://localhost:5432/baremaps?user=baremaps&password=baremaps' \
  --config 'tileset.json' \
  --style 'style.json'
```
