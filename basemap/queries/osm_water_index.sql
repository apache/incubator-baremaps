CREATE INDEX water_polygons_geometry_index ON water_polygons_shp USING SPGIST(geometry);
