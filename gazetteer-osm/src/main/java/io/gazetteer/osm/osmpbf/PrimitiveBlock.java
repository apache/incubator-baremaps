package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.domain.Node;
import io.gazetteer.osm.domain.Relation;
import io.gazetteer.osm.domain.Way;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class PrimitiveBlock {

    private final List<Node> nodes;

    private final List<Way> ways;

    private final List<Relation> relations;

    public PrimitiveBlock(List<Node> nodes, List<Way> ways, List<Relation> relations) {
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


}
