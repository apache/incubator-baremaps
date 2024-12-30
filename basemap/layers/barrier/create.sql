CREATE OR REPLACE VIEW osm_barrier AS
SELECT id, tags, geom FROM osm_way WHERE tags ? 'barrier';