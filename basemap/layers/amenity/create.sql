CREATE VIEW amenity AS
SELECT id, tags, geom FROM osm_ways WHERE tags ? 'amenity';