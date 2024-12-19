CREATE VIEW aerialway AS
SELECT id, tags, geom FROM osm_ways WHERE tags ? 'aerialway';