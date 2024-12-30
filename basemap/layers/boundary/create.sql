CREATE OR REPLACE VIEW osm_boundary AS
SELECT id, tags, geom FROM osm_way WHERE tags ? 'boundary';