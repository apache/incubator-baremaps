DROP MATERIALIZED VIEW IF EXISTS osm_named_entities CASCADE;

CREATE MATERIALIZED VIEW osm_named_entities AS
    SELECT id, 1 AS type, tags AS tags, to_tsvector('English', tags::text) AS tsv, st_envelope(geom) AS geom
    FROM osm_nodes
    WHERE tags ? 'name' AND geom IS NOT NULL
    UNION ALL
    SELECT id, 2 AS type, tags AS tags, to_tsvector('English', tags::text) AS tsv, st_envelope(geom) AS geom
    FROM osm_ways
    WHERE tags ? 'name' AND geom IS NOT NULL
    UNION ALL
    SELECT id, 3 AS type, tags AS tags, to_tsvector('English', tags::text) AS tsv, st_envelope(geom) AS geom
    FROM osm_relations
    WHERE tags ? 'name' AND geom IS NOT NULL;

CREATE INDEX osm_named_entities_gin
    ON osm_named_entities
    USING gin(tsv);

CREATE INDEX osm_named_entities_gist
    ON osm_named_entities
    USING gist(geom);