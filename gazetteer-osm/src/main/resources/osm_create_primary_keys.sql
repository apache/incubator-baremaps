ALTER TABLE osm_nodes
    ADD CONSTRAINT osm_nodes_pk PRIMARY KEY (id);
ALTER TABLE osm_ways
    ADD CONSTRAINT osm_ways_pk PRIMARY KEY (id);
ALTER TABLE osm_relations
    ADD CONSTRAINT osm_relations_pk PRIMARY KEY (id);
