CREATE VIEW aeroway AS
SELECT id, tags, geom FROM osm_ways WHERE tags ? 'aeroway'