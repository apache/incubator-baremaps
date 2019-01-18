package io.gazetteer.osm.osmpbf;

import org.junit.Test;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import static io.gazetteer.osm.osmpbf.FileBlockConstants.TEN_BLOCKS;
import static org.junit.Assert.*;

public class PrimitiveBlockReaderTest {

    @Test
    public void read() throws IOException {
        FileBlockReader fileBlockReader = FileBlocks.reader(TEN_BLOCKS);
        FileBlock headerBlock = fileBlockReader.read();
        PrimitiveBlockReader primitiveHeaderBlockReader = new PrimitiveBlockReader(Osmformat.PrimitiveBlock.parseFrom(headerBlock.getData()));
        PrimitiveBlock primitiveHeaderBlock = primitiveHeaderBlockReader.read();
        assertNotNull(primitiveHeaderBlock);
        assertTrue(primitiveHeaderBlock.getNodes().isEmpty());
        assertTrue(primitiveHeaderBlock.getWays().isEmpty());
        assertTrue(primitiveHeaderBlock.getRelations().isEmpty());
    }

}