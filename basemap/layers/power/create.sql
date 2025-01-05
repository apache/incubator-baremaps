CREATE OR REPLACE VIEW osm_power AS
SELECT
    id,
    jsonb_build_object(
        'power', tags -> 'power'
    ) AS tags,
    geom
FROM osm_way
WHERE geom IS NOT NULL
  AND tags ->> 'power' IN ('cable', 'line', 'minor_line', 'plant', 'substation');