CREATE INDEX osm_relations_tags_index ON osm_relations USING gin (tags);
CREATE INDEX osm_relations_geom_index ON osm_relations USING spgist (geom);
