package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.Entity;
import io.gazetteer.osm.util.Accumulator;
import org.junit.Test;

import java.util.Spliterator;

import static io.gazetteer.osm.OSMTestUtil.OSM_XML_DATA;
import static org.junit.Assert.*;

public class EntitySpliteratorTest {

    @Test
    public void tryAdvance() throws Exception {
        Spliterator<Entity> spliterator = EntityUtil.spliterator(OSM_XML_DATA);
        for (int i = 0; i < 10; i++) {
            assertTrue(spliterator.tryAdvance(block -> {
            }));
        }
        assertFalse(spliterator.tryAdvance(block -> {
        }));
    }

    @Test
    public void forEachRemaining() throws Exception {
        Spliterator<Entity> spliterator = EntityUtil.spliterator(OSM_XML_DATA);
        Accumulator<Entity> accumulator = new Accumulator<>();
        spliterator.forEachRemaining(accumulator);
        assertEquals(accumulator.acc.size(), 10);
    }
}