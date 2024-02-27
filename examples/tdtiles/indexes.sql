CREATE INDEX IF NOT EXISTS osm_ways_gin ON osm_ways USING gin (nodes);
CREATE INDEX IF NOT EXISTS osm_ways_tags_gin ON osm_ways USING gin (tags);
CREATE INDEX IF NOT EXISTS osm_relations_gin ON osm_relations USING gin (member_refs);
CREATE INDEX IF NOT EXISTS osm_nodes_gix ON osm_nodes USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_ways_gix ON osm_ways USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_relations_gix ON osm_relations USING GIST (geom);