package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.util.Accumulator;
import org.junit.Test;

import java.io.FileNotFoundException;

import static io.gazetteer.osm.OSMTestUtil.OSM_PBF_DATA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileBlockSpliteratorTest {

    @Test
    public void tryAdvance() throws FileNotFoundException {
        FileBlockSpliterator spliterator = PBFUtil.spliterator(OSM_PBF_DATA);
        for (int i = 0; i < 10; i++) {
            assertTrue(spliterator.tryAdvance(block -> {
            }));
        }
        assertFalse(spliterator.tryAdvance(block -> {
        }));
    }

    @Test
    public void forEachRemaining() throws FileNotFoundException {
        FileBlockSpliterator spliterator = PBFUtil.spliterator(OSM_PBF_DATA);
        Accumulator<FileBlock> accumulator = new Accumulator<>();
        spliterator.forEachRemaining(accumulator);
        assertTrue(accumulator.acc.size() == 10);
    }
}