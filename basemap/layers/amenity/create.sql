CREATE OR REPLACE VIEW osm_amenity AS
SELECT id, tags, geom FROM osm_way WHERE tags ? 'amenity';