CREATE OR REPLACE VIEW osm_power AS
SELECT id, tags, geom
FROM osm_way
WHERE geom IS NOT NULL
  AND tags ->> 'power' IN ('cable', 'line', 'minor_line', 'plant', 'substation');