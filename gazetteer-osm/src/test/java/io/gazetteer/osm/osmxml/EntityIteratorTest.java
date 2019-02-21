package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.model.Entity;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static io.gazetteer.osm.OSMTestUtil.osmPbfData;
import static org.junit.jupiter.api.Assertions.*;

public class EntityIteratorTest {

  @Test
  public void next() throws Exception {
    Iterator<Entity> reader = EntityUtil.iterator(osmPbfData());
    while (reader.hasNext()) {
      Entity block = reader.next();
      assertNotNull(block);
    }
    assertFalse(reader.hasNext());
  }

  @Test
  public void nextException() {
    assertThrows(Exception.class, () -> {
      Iterator<Entity> reader = EntityUtil.iterator(osmPbfData());
      while (reader.hasNext()) {
        reader.next();
      }
      reader.next();
    });
  }
}
