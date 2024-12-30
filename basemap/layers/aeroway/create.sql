CREATE OR REPLACE VIEW osm_aeroway AS
SELECT id, tags, geom FROM osm_way WHERE tags ? 'aeroway'