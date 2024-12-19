CREATE VIEW boundary AS
SELECT id, tags, geom FROM osm_ways WHERE tags ? 'boundary';