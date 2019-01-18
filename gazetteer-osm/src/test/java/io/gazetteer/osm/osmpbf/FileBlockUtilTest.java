package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.util.WrappedException;
import org.junit.Test;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Collectors;

import static io.gazetteer.osm.osmpbf.FileBlockConstants.INVALID_BLOCK;
import static io.gazetteer.osm.osmpbf.FileBlockConstants.TEN_BLOCKS;
import static org.junit.Assert.assertTrue;

public class FileBlockUtilTest {

    @Test
    public void stream() throws FileNotFoundException {
        assertTrue(FileBlockUtil
                .stream(TEN_BLOCKS)
                .count() == 10);
    }

    @Test
    public void isHeaderBlock() throws IOException {
        assertTrue(FileBlockUtil
                .stream(TEN_BLOCKS)
                .filter(FileBlockUtil::isHeaderBlock)
                .count() == 1);
    }

    @Test
    public void isDataBlock() throws IOException {
        assertTrue(FileBlockUtil
                .stream(TEN_BLOCKS)
                .filter(FileBlockUtil::isDataBlock)
                .count() == 9);
    }

    @Test
    public void toHeaderBlock() throws FileNotFoundException {
        assertTrue(FileBlockUtil
                .stream(TEN_BLOCKS)
                .filter(FileBlockUtil::isHeaderBlock)
                .map(FileBlockUtil::toHeaderBlock)
                .count() == 1);
    }

    @Test
    public void toDataBlock() throws FileNotFoundException {
        assertTrue(FileBlockUtil
                .stream(TEN_BLOCKS)
                .filter(FileBlockUtil::isDataBlock)
                .map(FileBlockUtil::toDataBlock)
                .collect(Collectors.toList()).size() == 9);
    }

    @Test(expected = WrappedException.class)
    public void toDataBlockException() {
        FileBlockUtil.toDataBlock(INVALID_BLOCK);
    }

}
