CREATE INDEX CONCURRENTLY osm_nodes_gix ON osm_nodes USING GIST (geom);
CREATE INDEX CONCURRENTLY osm_ways_gix ON osm_ways USING GIST (geom);
CREATE INDEX CONCURRENTLY osm_relations_gix ON osm_relations USING GIST (geom);