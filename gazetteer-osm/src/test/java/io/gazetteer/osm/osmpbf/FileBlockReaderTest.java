package io.gazetteer.osm.osmpbf;

import org.junit.Test;

import java.io.*;

import static io.gazetteer.osm.osmpbf.FileBlockConstants.TEN_BLOCKS;
import static org.junit.Assert.*;

public class FileBlockReaderTest {

    @Test
    public void testRead() throws IOException {
        FileBlockReader reader = FileBlockUtil.reader(TEN_BLOCKS);
        for (int i = 0; i < 10; i ++) {
            FileBlock block = reader.read();
            assertNotNull(block);
        }
    }

    @Test(expected = EOFException.class)
    public void testEOF() throws IOException {
        FileBlockReader reader = FileBlockUtil.reader(TEN_BLOCKS);
        for (int i = 0; i < 11; i ++) {
            reader.read();
        }
    }

}