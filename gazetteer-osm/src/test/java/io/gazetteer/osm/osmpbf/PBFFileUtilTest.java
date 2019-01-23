package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.util.WrappedException;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Collectors;

import static io.gazetteer.osm.OSMTestUtil.PBF_INVALID_BLOCK;
import static io.gazetteer.osm.OSMTestUtil.PBF_DATA;
import static org.junit.Assert.assertTrue;

public class PBFFileUtilTest {

    @Test
    public void stream() throws FileNotFoundException {
        assertTrue(PBFFileUtil
                .stream(PBF_DATA)
                .count() == 10);
    }

    @Test
    public void isHeaderBlock() throws IOException {
        assertTrue(PBFFileUtil
                .stream(PBF_DATA)
                .filter(PBFFileUtil::isHeaderBlock)
                .count() == 1);
    }

    @Test
    public void isDataBlock() throws IOException {
        assertTrue(PBFFileUtil
                .stream(PBF_DATA)
                .filter(PBFFileUtil::isDataBlock)
                .count() == 9);
    }

    @Test
    public void toHeaderBlock() throws FileNotFoundException {
        assertTrue(PBFFileUtil
                .stream(PBF_DATA)
                .filter(PBFFileUtil::isHeaderBlock)
                .map(PBFFileUtil::toHeaderBlock)
                .count() == 1);
    }

    @Test
    public void toDataBlock() throws FileNotFoundException {
        assertTrue(PBFFileUtil
                .stream(PBF_DATA)
                .filter(PBFFileUtil::isDataBlock)
                .map(PBFFileUtil::toDataBlock)
                .collect(Collectors.toList()).size() == 9);
    }

    @Test(expected = WrappedException.class)
    public void toDataBlockException() {
        PBFFileUtil.toDataBlock(PBF_INVALID_BLOCK);
    }

}
