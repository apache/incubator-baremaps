package gazetteer.osm.osmpbf;

import gazetteer.osm.domain.Node;
import gazetteer.osm.domain.Relation;
import gazetteer.osm.domain.Way;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Stream;

public class PrimitiveBlocks {

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

    public static Stream<PrimitiveBlockReader> stream(File file) throws FileNotFoundException {
        return FileBlocks.stream(file)
                .filter(FileBlocks::isDataBlock)
                .map(FileBlocks::toDataBlock)
                .map(PrimitiveBlockReader::new);
    }

}
