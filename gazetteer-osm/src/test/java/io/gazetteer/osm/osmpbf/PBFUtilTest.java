package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.util.WrappedException;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Collectors;

import static io.gazetteer.osm.OSMTestUtil.OSM_PBF_INVALID_BLOCK;
import static io.gazetteer.osm.OSMTestUtil.OSM_PBF_DATA;
import static org.junit.Assert.assertTrue;

public class PBFUtilTest {

    @Test
    public void stream() throws FileNotFoundException {
        assertTrue(PBFUtil
                .fileBlocks(OSM_PBF_DATA)
                .count() == 10);
    }

    @Test
    public void isHeaderBlock() throws IOException {
        assertTrue(PBFUtil
                .fileBlocks(OSM_PBF_DATA)
                .filter(PBFUtil::isHeaderBlock)
                .count() == 1);
    }

    @Test
    public void isDataBlock() throws IOException {
        assertTrue(PBFUtil
                .fileBlocks(OSM_PBF_DATA)
                .filter(PBFUtil::isDataBlock)
                .count() == 9);
    }

    @Test
    public void toHeaderBlock() throws FileNotFoundException {
        assertTrue(PBFUtil
                .fileBlocks(OSM_PBF_DATA)
                .filter(PBFUtil::isHeaderBlock)
                .map(PBFUtil::toHeaderBlock)
                .count() == 1);
    }

    @Test
    public void toDataBlock() throws FileNotFoundException {
        assertTrue(PBFUtil
                .fileBlocks(OSM_PBF_DATA)
                .filter(PBFUtil::isDataBlock)
                .map(PBFUtil::toDataBlock)
                .collect(Collectors.toList()).size() == 9);
    }

    @Test(expected = WrappedException.class)
    public void toDataBlockException() {
        PBFUtil.toDataBlock(OSM_PBF_INVALID_BLOCK);
    }

}
