package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.Entity;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static io.gazetteer.osm.OSMTestUtil.OSM_PBF_DATA;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertFalse;

public class EntityIteratorTest {
    
    @Test
    public void next() throws Exception {
        Iterator<Entity> reader = EntityUtil.iterator(OSM_PBF_DATA);
        while (reader.hasNext()) {
            Entity block = reader.next();
            assertNotNull(block);
        }
        assertFalse(reader.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void nextException() throws Exception {
        Iterator<Entity> reader = EntityUtil.iterator(OSM_PBF_DATA);
        while (reader.hasNext()) {
            reader.next();
        }
        reader.next();
    }

}

