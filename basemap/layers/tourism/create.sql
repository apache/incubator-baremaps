CREATE VIEW osm_tourism AS
SELECT id, tags, geom FROM osm_relations_z$zoom WHERE tags ? 'tourism';