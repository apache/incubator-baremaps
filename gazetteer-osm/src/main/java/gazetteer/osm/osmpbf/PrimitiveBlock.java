package gazetteer.osm.osmpbf;

import gazetteer.osm.domain.Node;
import gazetteer.osm.domain.Relation;
import gazetteer.osm.domain.Way;

import java.util.List;

public class PrimitiveBlock {

    private final List<Node> nodes;

    private final List<Way> ways;

    private final List<Relation> relations;

    public PrimitiveBlock(List<Node> nodes, List<Way> ways, List<Relation> relations) {
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
