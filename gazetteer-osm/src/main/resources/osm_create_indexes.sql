CREATE INDEX osm_nodes_gix ON osm_nodes USING GIST (geom);
CREATE INDEX osm_ways_gix ON osm_ways USING GIST (geom);
CREATE INDEX osm_relations_gix ON osm_relations USING GIST (geom);
CREATE INDEX osm_ways_gin ON osm_ways USING gin (nodes);
CREATE INDEX osm_relations_gin ON osm_relations USING gin (member_refs);