package io.gazetteer.osm.postgis;

import io.gazetteer.osm.domain.Relation;

import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class RelationMapping extends GeometryMapping<Relation> {

    private final ToLongFunction<Relation> getId = relation -> relation.getInfo().getId();

    private final ToIntFunction<Relation> getVersion = relation -> relation.getInfo().getVersion();

    private final ToLongFunction<Relation> getTimestamp = relation -> relation.getInfo().getChangeset();

    private final ToLongFunction<Relation> getChangeset = relation -> relation.getInfo().getChangeset();

    private final ToIntFunction<Relation> getUserId = relation -> relation.getInfo().getUser().getId();

    private final Function<Relation, Map<String, String>> getTags = relation -> relation.getInfo().getTags();

    public RelationMapping() {
        super("public", "osm_relations");
        mapLong("id", getId);
        mapInteger("version", getVersion);
        mapInteger("uid", getUserId);
        mapLong("timestamp", getTimestamp);
        mapLong("changeset", getChangeset);
        mapHstore("tags", getTags);
        mapGeometry("geom", relation -> {
            return null;
        });
    }

}
