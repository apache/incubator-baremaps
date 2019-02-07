package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.model.Entity;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static io.gazetteer.osm.OSMTestUtil.OSM_PBF_DATA;
import static org.junit.jupiter.api.Assertions.*;

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

  @Test
  public void nextException() throws Exception {
    assertThrows(NoSuchElementException.class, () -> {
      Iterator<Entity> reader = EntityUtil.iterator(OSM_PBF_DATA);
      while (reader.hasNext()) {
        reader.next();
      }
      reader.next();
    });
  }
}
