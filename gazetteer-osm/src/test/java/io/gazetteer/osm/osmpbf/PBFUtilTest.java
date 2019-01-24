package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.util.Accumulator;
import io.gazetteer.osm.util.WrappedException;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Spliterator;
import java.util.stream.Collectors;

import static io.gazetteer.osm.OSMTestUtil.OSM_PBF_DATA;
import static io.gazetteer.osm.OSMTestUtil.OSM_PBF_INVALID_BLOCK;
import static org.junit.Assert.assertFalse;
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

    @Test
    public void tryAdvance() throws FileNotFoundException {
        Spliterator<FileBlock> spliterator = PBFUtil.spliterator(OSM_PBF_DATA);
        for (int i = 0; i < 10; i++) {
            assertTrue(spliterator.tryAdvance(block -> {
            }));
        }
        assertFalse(spliterator.tryAdvance(block -> {
        }));
    }

    @Test
    public void forEachRemaining() throws FileNotFoundException {
        Spliterator<FileBlock> spliterator = PBFUtil.spliterator(OSM_PBF_DATA);
        Accumulator<FileBlock> accumulator = new Accumulator<>();
        spliterator.forEachRemaining(accumulator);
        assertTrue(accumulator.acc.size() == 10);
    }

}
