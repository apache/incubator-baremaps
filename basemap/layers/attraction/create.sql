CREATE VIEW attraction AS
SELECT id, tags, geom FROM osm_ways WHERE tags ? 'attraction';