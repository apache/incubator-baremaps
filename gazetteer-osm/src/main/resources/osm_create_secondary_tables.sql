DROP TABLE IF EXISTS osm_ways_geom;

CREATE TABLE osm_ways_geom AS (
    SELECT w.id, w.version, w.uid, w.timestamp, w.changeset, w.tags, w.nodes, g.geom
    FROM osm_ways w
    JOIN (
        SELECT g.id, st_makeline(g.geom) AS geom
        FROM (
            SELECT w.id, n.geom
            FROM osm_ways w, unnest(w.nodes) WITH ORDINALITY as node
            JOIN osm_nodes n ON node = n.id
            ORDER BY w.id, ordinality
        ) AS g
        GROUP BY g.id
    ) AS g ON w.id = g.id
);

ALTER TABLE osm_ways_geom ADD PRIMARY KEY (id);