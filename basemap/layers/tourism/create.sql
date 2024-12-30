CREATE VIEW osm_tourism AS
SELECT id, tags, geom FROM osm_relation WHERE tags ? 'tourism';