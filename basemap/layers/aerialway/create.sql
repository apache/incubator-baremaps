CREATE OR REPLACE VIEW osm_aerialway AS
SELECT id, tags, geom FROM osm_way WHERE tags ? 'aerialway';