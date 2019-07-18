-- A trigger that updates the geometry column of a way after an insertion or an update.
DROP FUNCTION IF EXISTS osm_ways_update_geometry();
CREATE FUNCTION osm_ways_update_geometry() RETURNS trigger AS $$
    DECLARE
        ge geometry;
    BEGIN
        SELECT CASE
                WHEN st_isclosed(g.geom) THEN st_makepolygon(g.geom)
                ELSE g.geom END INTO ge
        FROM osm_ways w JOIN (
            SELECT w.id, st_makeline(n.geom ORDER BY ordinality) as geom
            FROM osm_ways w, unnest(w.nodes) WITH ORDINALITY as node JOIN osm_nodes n ON node = n.id
            GROUP BY w.id
        ) AS g ON w.id = g.id
        WHERE w.id = NEW.id;
        UPDATE osm_ways SET geom = ge WHERE id = NEW.id;
        RETURN NEW;
    END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS osm_ways_trigger_insert ON osm_ways;
CREATE TRIGGER osm_ways_trigger_insert
AFTER INSERT ON osm_ways
FOR EACH ROW
EXECUTE PROCEDURE osm_ways_update_geometry();

DROP TRIGGER IF EXISTS osm_ways_trigger_update ON osm_ways;
CREATE TRIGGER osm_ways_trigger_update
    AFTER UPDATE ON osm_ways
    FOR EACH ROW
    WHEN (OLD.version <> NEW.version)
EXECUTE PROCEDURE osm_ways_update_geometry();

-- A trigger that updates the geometry column of a relation after an insertion or an update.
DROP FUNCTION IF EXISTS osm_relations_update_geometry();
CREATE FUNCTION osm_relations_update_geometry() RETURNS trigger AS $$
    DECLARE
        ge geometry;
    BEGIN
        SELECT CASE
                WHEN r.tags -> 'type' = 'multipolygon' THEN st_buildarea(st_makevalid(st_collect(w.geom ORDER BY ordinality)))
                -- Add cases for other types here
                ELSE NULL END INTO ge
        FROM osm_relations r, unnest(r.member_refs) WITH ORDINALITY as way JOIN osm_ways w ON way = w.id
        GROUP BY r.id, r.tags, NEW.id
        HAVING r.id = NEW.id;
        UPDATE osm_relations SET geom = ge WHERE id = NEW.id;
        RETURN NEW;
    END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS osm_relations_trigger_insert ON osm_relations;
CREATE TRIGGER osm_relations_trigger_insert
AFTER INSERT ON osm_relations
FOR EACH ROW
EXECUTE PROCEDURE osm_relations_update_geometry();

DROP TRIGGER IF EXISTS osm_relations_trigger_update ON osm_relations;
CREATE TRIGGER osm_relations_trigger_update
AFTER UPDATE ON osm_relations
FOR EACH ROW
WHEN (OLD.version <> NEW.version)
EXECUTE PROCEDURE osm_relations_update_geometry();