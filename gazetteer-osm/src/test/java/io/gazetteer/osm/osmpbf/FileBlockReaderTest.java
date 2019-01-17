package io.gazetteer.osm.osmpbf;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class FileBlockReaderTest {

    @Test
    public void testRead() throws IOException {
        String file = getClass().getClassLoader().getResource("10_blocks.osm").getPath();
        System.out.println(file);
        DataInputStream input = new DataInputStream(new FileInputStream(new File(file)));
        FileBlockReader reader = new FileBlockReader(input);
        for (int i = 0; i < 10; i ++) {
            FileBlock block = reader.read();
            assertNotNull(block);
        }
    }

    @Test(expected = EOFException.class)
    public void testEOF() throws IOException {
        String file = getClass().getClassLoader().getResource("10_blocks.osm").getPath();
        System.out.println(file);
        DataInputStream input = new DataInputStream(new FileInputStream(new File(file)));
        FileBlockReader reader = new FileBlockReader(input);
        for (int i = 0; i < 11; i ++) {
            reader.read();
        }
    }

}