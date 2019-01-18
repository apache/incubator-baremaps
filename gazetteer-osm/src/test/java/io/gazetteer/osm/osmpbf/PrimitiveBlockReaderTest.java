package io.gazetteer.osm.osmpbf;

import org.junit.Test;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

import java.io.IOException;

import static io.gazetteer.osm.osmpbf.FileBlockConstants.TEN_BLOCKS;
import static org.junit.Assert.*;

public class PrimitiveBlockReaderTest {

    @Test
    public void read() throws IOException {
        FileBlockReader fileBlockReader = FileBlockUtil.reader(TEN_BLOCKS);
        FileBlock headerBlock = fileBlockReader.read();
        PrimitiveBlockReader primitiveBlockReader = new PrimitiveBlockReader(Osmformat.PrimitiveBlock.parseFrom(headerBlock.getData()));
        PrimitiveBlock primitiveBlock = primitiveBlockReader.read();
        assertNotNull(primitiveBlock);
        assertTrue(primitiveBlock.getNodes().isEmpty());
        assertTrue(primitiveBlock.getWays().isEmpty());
        assertTrue(primitiveBlock.getRelations().isEmpty());
    }

}