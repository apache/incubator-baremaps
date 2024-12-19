CREATE VIEW power AS
SELECT id, tags, geom FROM osm_ways WHERE tags ->> 'power' IN ('cable', 'line', 'minor_line', 'plant', 'substation');