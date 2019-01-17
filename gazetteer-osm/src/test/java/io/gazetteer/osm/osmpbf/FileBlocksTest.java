package io.gazetteer.osm.osmpbf;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Collectors;

import static io.gazetteer.osm.osmpbf.FileBlockConstants.TEN_BLOCKS;
import static org.junit.Assert.assertTrue;

public class FileBlocksTest {

    @Test
    public void stream() throws FileNotFoundException {
        assertTrue(FileBlocks
                .stream(TEN_BLOCKS)
                .count() == 10);
    }

    @Test
    public void isHeaderBlock() throws IOException {
        assertTrue(FileBlocks
                .stream(TEN_BLOCKS)
                .filter(FileBlocks::isHeaderBlock)
                .count() == 1);
    }

    @Test
    public void isDataBlock() throws IOException {
        assertTrue(FileBlocks
                .stream(TEN_BLOCKS)
                .filter(FileBlocks::isDataBlock)
                .count() == 9);
    }

    @Test
    public void toHeaderBlock() throws FileNotFoundException {
        assertTrue(FileBlocks
                .stream(TEN_BLOCKS)
                .filter(FileBlocks::isHeaderBlock)
                .map(FileBlocks::toHeaderBlock)
                .count() == 1);
    }

    @Test
    public void toDataBlock() throws FileNotFoundException {
        assertTrue(FileBlocks
                .stream(TEN_BLOCKS)
                .filter(FileBlocks::isDataBlock)
                .map(FileBlocks::toDataBlock)
                .collect(Collectors.toList()).size() == 9);
    }

}
