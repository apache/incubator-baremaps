package gazetteer.osm.osmpbf;

import gazetteer.osm.model.Node;
import gazetteer.osm.model.Relation;
import gazetteer.osm.model.Way;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

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

    public static PrimitiveBlock read(Osmformat.PrimitiveBlock block) {
        return new PrimitiveBlockReader(block).read();
    }

    public static List<Node> readDenseNodes(Osmformat.PrimitiveBlock block) {
        return new PrimitiveBlockReader(block).readDenseNodes();
    }

    public static List<Node> readNodes(Osmformat.PrimitiveBlock block) {
        return new PrimitiveBlockReader(block).readNodes();
    }

    public static List<Way> readWays(Osmformat.PrimitiveBlock block) {
        return new PrimitiveBlockReader(block).readWays();
    }

    public static List<Relation> readRelations(Osmformat.PrimitiveBlock block) {
        return new PrimitiveBlockReader(block).readRelations();
    }

}
