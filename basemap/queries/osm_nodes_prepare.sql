CREATE INDEX IF NOT EXISTS osm_nodes_tags_index ON osm_nodes USING gin (tags);
CREATE INDEX IF NOT EXISTS osm_nodes_geom_index ON osm_nodes USING spgist (geom);
