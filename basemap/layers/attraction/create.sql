CREATE VIEW osm_attraction AS
SELECT id, tags, geom FROM osm_way WHERE tags ? 'attraction';