package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.Entity;
import io.gazetteer.osm.osmpbf.FileBlock;
import io.gazetteer.osm.osmpbf.FileBlockSpliterator;
import io.gazetteer.osm.osmpbf.PBFUtil;
import io.gazetteer.osm.util.Accumulator;
import org.junit.Test;

import java.io.FileNotFoundException;

import static io.gazetteer.osm.OSMTestUtil.PBF_DATA;
import static io.gazetteer.osm.OSMTestUtil.XML_DATA;
import static org.junit.Assert.*;

public class EntitySpliteratorTest {

    @Test
    public void tryAdvance() throws Exception {
        EntitySpliterator spliterator = XMLUtil.spliterator(XML_DATA);
        for (int i = 0; i < 10; i++) {
            assertTrue(spliterator.tryAdvance(block -> {
            }));
        }
        assertFalse(spliterator.tryAdvance(block -> {
        }));
    }

    @Test
    public void forEachRemaining() throws Exception {
        EntitySpliterator spliterator = XMLUtil.spliterator(XML_DATA);
        Accumulator<Entity> accumulator = new Accumulator<>();
        spliterator.forEachRemaining(accumulator);
        assertTrue(accumulator.acc.size() == 10);
    }
}