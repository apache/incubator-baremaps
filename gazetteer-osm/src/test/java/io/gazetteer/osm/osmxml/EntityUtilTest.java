package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.model.Entity;
import io.gazetteer.common.stream.AccumulatingConsumer;
import org.junit.jupiter.api.Test;

import java.util.Spliterator;

import static io.gazetteer.osm.OSMTestUtil.osmXmlData;
import static org.junit.jupiter.api.Assertions.*;

public class EntityUtilTest {

  @Test
  public void tryAdvance() throws Exception {
    Spliterator<Entity> spliterator = EntityUtil.spliterator(osmXmlData());
    for (int i = 0; i < 10; i++) {
      assertTrue(spliterator.tryAdvance(block -> {}));
    }
    assertFalse(spliterator.tryAdvance(block -> {}));
  }

  @Test
  public void forEachRemaining() throws Exception {
    Spliterator<Entity> spliterator = EntityUtil.spliterator(osmXmlData());
    AccumulatingConsumer<Entity> accumulator = new AccumulatingConsumer<>();
    spliterator.forEachRemaining(accumulator);
    assertEquals(accumulator.values().size(), 10);
  }
}
