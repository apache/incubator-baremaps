DROP TABLE IF EXISTS osm_ways_geom;
CREATE TABLE osm_ways_geom AS (
    SELECT w.id, w.version, w.uid, w.timestamp, w.changeset, w.tags, w.nodes, CASE WHEN st_isclosed(g.geom) THEN st_makepolygon(g.geom) ELSE g.geom END AS geom
    FROM osm_ways w JOIN (
        SELECT w.id, st_makeline(n.geom ORDER BY ordinality) as geom
        FROM osm_ways w, unnest(w.nodes) WITH ORDINALITY as node JOIN osm_nodes n ON node = n.id
        GROUP BY w.id
    ) AS g ON w.id = g.id
);
DROP TABLE IF EXISTS osm_ways;
ALTER TABLE osm_ways_geom RENAME TO osm_ways;
ALTER TABLE osm_ways ADD PRIMARY KEY (id);

DROP TABLE IF EXISTS osm_relations_geom;
CREATE TABLE osm_relations_geom AS (
    SELECT r.id, r.version, r.uid, r.timestamp, r.changeset, r.tags, r.member_refs, r.member_types, r.member_roles, st_buildarea(st_makevalid(g.geom)) AS geom
    FROM osm_relations r JOIN (
        SELECT r.id, st_collect(w.geom ORDER BY ordinality) as geom
        FROM osm_relations r, unnest(r.member_refs) WITH ORDINALITY as way JOIN osm_ways w ON way = w.id
        -- WHERE r.tags -> 'type' = 'multipolygon' -- TODO: find a way to remove this limitation
        GROUP BY r.id
    ) AS g ON r.id = g.id
);
DROP TABLE IF EXISTS osm_relations;
ALTER TABLE osm_relations_geom RENAME TO osm_relations;
ALTER TABLE osm_relations ADD PRIMARY KEY (id);