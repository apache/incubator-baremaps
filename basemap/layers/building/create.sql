CREATE OR REPLACE VIEW osm_building AS
SELECT
    id,
    tags
        || jsonb_build_object('extrusion:base',
                              CASE
                                  WHEN tags ? 'min_height'
                                      THEN convert_to_number(tags ->> 'min_height', 0)
                                  WHEN tags ? 'building:min_height'
                                      THEN convert_to_number(tags ->> 'building:min_height', 0)
                                  WHEN tags ? 'building:min_level'
                                      THEN convert_to_number(tags ->> 'building:min_level', 0) * 3
                                  ELSE 0
                                  END)
        || jsonb_build_object('extrusion:height',
                              CASE
                                  WHEN tags ? 'height'
                                      THEN convert_to_number(tags ->> 'height', 6)
                                  WHEN tags ? 'building:height'
                                      THEN convert_to_number(tags ->> 'building:height', 6)
                                  WHEN tags ? 'building:levels'
                                      THEN convert_to_number(tags ->> 'building:levels', 2) * 3
                                  ELSE 6
                                  END) as tags,
    geom
FROM osm_polygon
WHERE (tags ? 'building' OR tags ? 'building:part')
  AND ((NOT tags ? 'layer') OR convert_to_number(tags ->> 'layer', 0) >= 0)