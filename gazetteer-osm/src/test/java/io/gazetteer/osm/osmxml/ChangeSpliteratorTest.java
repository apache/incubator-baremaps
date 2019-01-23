package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.Change;
import io.gazetteer.osm.domain.Entity;
import io.gazetteer.osm.util.Accumulator;
import org.junit.Test;

import static io.gazetteer.osm.OSMTestUtil.OSC_XML_DATA;
import static io.gazetteer.osm.OSMTestUtil.OSM_XML_DATA;
import static org.junit.Assert.*;

public class ChangeSpliteratorTest {

    @Test
    public void tryAdvance() throws Exception {
        ChangeSpliterator spliterator = XMLUtil.changeSpliterator(OSC_XML_DATA);
        for (int i = 0; i < 51; i++) {
            assertTrue(spliterator.tryAdvance(block -> {
            }));
        }
        assertFalse(spliterator.tryAdvance(block -> {
        }));
    }

    @Test
    public void forEachRemaining() throws Exception {
        ChangeSpliterator spliterator = XMLUtil.changeSpliterator(OSC_XML_DATA);
        Accumulator<Change> accumulator = new Accumulator<>();
        spliterator.forEachRemaining(accumulator);
        assertEquals(accumulator.acc.size(), 51);
    }
}