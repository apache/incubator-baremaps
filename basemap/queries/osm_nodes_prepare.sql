CREATE INDEX IF NOT EXISTS osm_nodes_tags_index ON osm_nodes USING gin (tags);
CREATE INDEX IF NOT EXISTS osm_nodes_geom_index ON osm_nodes USING spgist (geom);
CREATE INDEX IF NOT EXISTS osm_nodes_geom_fulltext_index ON osm_nodes USING gist ((to_tsvector('English', tags::text))) ;
