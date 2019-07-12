package io.gazetteer.osm.osmpbf;

import com.google.common.base.Objects;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.model.Way;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Data {

  private final List<Node> nodes;

  private final List<Way> ways;

  private final List<Relation> relations;

  public Data(List<Node> nodes, List<Way> ways, List<Relation> relations) {
    checkNotNull(nodes);
    checkNotNull(ways);
    checkNotNull(relations);
    this.nodes = nodes;
    this.ways = ways;
    this.relations = relations;
  }

  public List<Node> getNodes() {
    return nodes;
  }

  public List<Way> getWays() {
    return ways;
  }

  public List<Relation> getRelations() {
    return relations;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Data that = (Data) o;
    return Objects.equal(nodes, that.nodes)
        && Objects.equal(ways, that.ways)
        && Objects.equal(relations, that.relations);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(nodes, ways, relations);
  }
}
