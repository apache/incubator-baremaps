package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.util.WrappedException;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Collectors;

import static io.gazetteer.osm.osmpbf.FileBlockConstants.INVALID_BLOCK;
import static io.gazetteer.osm.osmpbf.FileBlockConstants.BLOCKS;
import static org.junit.Assert.assertTrue;

public class FileBlockUtilTest {

    @Test
    public void stream() throws FileNotFoundException {
        assertTrue(PBFFileUtil
                .stream(BLOCKS)
                .count() == 10);
    }

    @Test
    public void isHeaderBlock() throws IOException {
        assertTrue(PBFFileUtil
                .stream(BLOCKS)
                .filter(PBFFileUtil::isHeaderBlock)
                .count() == 1);
    }

    @Test
    public void isDataBlock() throws IOException {
        assertTrue(PBFFileUtil
                .stream(BLOCKS)
                .filter(PBFFileUtil::isDataBlock)
                .count() == 9);
    }

    @Test
    public void toHeaderBlock() throws FileNotFoundException {
        assertTrue(PBFFileUtil
                .stream(BLOCKS)
                .filter(PBFFileUtil::isHeaderBlock)
                .map(PBFFileUtil::toHeaderBlock)
                .count() == 1);
    }

    @Test
    public void toDataBlock() throws FileNotFoundException {
        assertTrue(PBFFileUtil
                .stream(BLOCKS)
                .filter(PBFFileUtil::isDataBlock)
                .map(PBFFileUtil::toDataBlock)
                .collect(Collectors.toList()).size() == 9);
    }

    @Test(expected = WrappedException.class)
    public void toDataBlockException() {
        PBFFileUtil.toDataBlock(INVALID_BLOCK);
    }

}
