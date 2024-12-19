CREATE MATERIALIZED VIEW osm_railway AS
SELECT id, tags, geom
FROM (SELECT min(id)                                                                        as id,
             jsonb_build_object('railway', tags -> 'railway', 'service', tags -> 'service') as tags,
             (st_dump(st_linemerge(st_collect(geom)))).geom                                 as geom
      FROM osm_ways
      WHERE tags ->> 'railway' IN ('light_rail', 'monorail', 'rail', 'subway', 'tram')
      GROUP BY tags -> 'railway', tags -> 'service') AS mergedDirective;