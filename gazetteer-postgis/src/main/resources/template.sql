SELECT
  {{#layers}}
    ST_AsMVT({{name}}, '{{name}}', 4096, 'geom'),
  {{/layers}}
FROM
  {{#layers}}
  (
    SELECT id, ST_AsMvtGeom(geom, ST_MakeEnvelope(8.262, 46.785, 8.284, 46.8), 4096, 256, true) AS geom
    FROM ({{sql}}) AS layer
    WHERE geom && ST_MakeEnvelope(8.262, 46.785, 8.284, 46.8)
    AND ST_Intersects(geom, ST_MakeEnvelope(8.262, 46.785, 8.284, 46.8))
  ) AS {{name}}
  {{/layers}}
