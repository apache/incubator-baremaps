CREATE FUNCTION osm_ways_update_geometry() RETURNS trigger AS $$
    DECLARE
        geom INTEGER;
    BEGIN
        SELECT CASE WHEN st_isclosed(g.geom) THEN st_makepolygon(g.geom) ELSE g.geom END INTO geom
        FROM osm_ways w JOIN (
            SELECT w.id, st_makeline(n.geom ORDER BY ordinality) as geom
            FROM osm_ways w, unnest(w.nodes) WITH ORDINALITY as node JOIN osm_nodes n ON node = n.id
            GROUP BY w.id
        ) AS g ON w.id = g.id;
        UPDATE osm_ways SET geom = geom WHERE id = NEW.id;
        RETURN NEW;
    END;
$$ LANGUAGE 'plpgsql';


CREATE TRIGGER osm_ways_trigger
AFTER INSERT OR UPDATE ON osm_ways
FOR EACH ROW
EXECUTE PROCEDURE osm_ways_update_geometry();