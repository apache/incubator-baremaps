package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.domain.Node;
import io.gazetteer.osm.domain.Relation;
import io.gazetteer.osm.domain.Way;
import org.junit.Test;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

import java.io.IOException;
import java.util.List;

import static io.gazetteer.osm.OSMTestUtil.*;
import static org.junit.Assert.*;

public class DataBlockReaderTest {

    @Test
    public void read() throws IOException {
        FileBlockReader fileBlockReader = PBFUtil.reader(OSM_PBF_DATA);
        FileBlock headerBlock = fileBlockReader.read();
        DataBlockReader primitiveBlockReader = new DataBlockReader(Osmformat.PrimitiveBlock.parseFrom(headerBlock.getData()));
        DataBlock primitiveBlock = primitiveBlockReader.read();
        assertNotNull(primitiveBlock);
        for (int i = 0; i < 9; i++) {
            primitiveBlock = primitiveBlockReader.read();
            assertNotNull(primitiveBlock);
        }
    }

    @Test
    public void readDenseNodes() throws IOException {
        FileBlockReader reader = PBFUtil.reader(OSM_PBF_DENSE_BLOCK);
        FileBlock block = reader.read();
        DataBlockReader primitiveBlockReader = new DataBlockReader(Osmformat.PrimitiveBlock.parseFrom(block.getData()));
        List<Node> nodes = primitiveBlockReader.readDenseNodes();
        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
    }

    @Test
    public void readWays() throws IOException {
        FileBlockReader reader = PBFUtil.reader(OSM_PBF_WAYS_BLOCK);
        FileBlock block = reader.read();
        DataBlockReader primitiveBlockReader = new DataBlockReader(Osmformat.PrimitiveBlock.parseFrom(block.getData()));
        List<Way> ways = primitiveBlockReader.readWays();
        assertNotNull(ways);
        assertFalse(ways.isEmpty());
    }

    @Test
    public void readRelations() throws IOException {
        FileBlockReader reader = PBFUtil.reader(OSM_PBF_RELATIONS_BLOCK);
        FileBlock block = reader.read();
        DataBlockReader primitiveBlockReader = new DataBlockReader(Osmformat.PrimitiveBlock.parseFrom(block.getData()));
        List<Relation> relations = primitiveBlockReader.readRelations();
        assertNotNull(relations);
        assertFalse(relations.isEmpty());
    }

}