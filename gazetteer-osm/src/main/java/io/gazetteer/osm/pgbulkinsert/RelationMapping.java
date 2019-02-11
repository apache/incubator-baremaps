package io.gazetteer.osm.pgbulkinsert;

import io.gazetteer.osm.model.Relation;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

public class RelationMapping extends GeometryMapping<Relation> {

  private final ToLongFunction<Relation> getId = relation -> relation.getInfo().getId();

  private final ToIntFunction<Relation> getVersion = relation -> relation.getInfo().getVersion();

  private final ToLongFunction<Relation> getTimestamp =
      relation -> relation.getInfo().getChangeset();

  private final ToLongFunction<Relation> getChangeset =
      relation -> relation.getInfo().getChangeset();

  private final ToIntFunction<Relation> getUserId =
      relation -> relation.getInfo().getUserId();

  private final Function<Relation, Map<String, String>> getTags =
      relation -> relation.getInfo().getTags();

  private final Function<Relation, Collection<Long>> getMemberRefs =
          relation -> relation.getMembers().stream().map(m -> m.getRef()).collect(Collectors.toList());

  private final Function<Relation, Collection<String>> getMemberTypes =
          relation -> relation.getMembers().stream().map(m -> m.getType().name()).collect(Collectors.toList());

  private final Function<Relation, Collection<String>> getMemberRoles =
          relation -> relation.getMembers().stream().map(m -> m.getRole()).collect(Collectors.toList());

  public RelationMapping() {
    super("public", "osm_relations");
    mapLong("id", getId);
    mapInteger("version", getVersion);
    mapInteger("uid", getUserId);
    mapLong("timestamp", getTimestamp);
    mapLong("changeset", getChangeset);
    mapHstore("tags", getTags);
    mapLongArray("member_refs", getMemberRefs);
    mapTextArray("member_types", getMemberTypes);
    mapTextArray("member_roles", getMemberRoles);

  }
}
