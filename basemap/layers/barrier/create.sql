CREATE VIEW barrier AS
SELECT id, tags, geom FROM osm_ways WHERE tags ? 'barrier';