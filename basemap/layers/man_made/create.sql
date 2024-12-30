CREATE OR REPLACE VIEW osm_man_made AS
SELECT id, tags, geom FROM osm_way
WHERE tags ? 'man_made'