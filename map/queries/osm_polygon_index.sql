CREATE INDEX osm_polygon_geom_idx ON osm_polygon USING GIST (geom);
CREATE INDEX osm_polygon_tags_idx ON osm_polygon USING GIN (tags);

