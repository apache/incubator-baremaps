package io.gazetteer.osm.osmxml;

import static io.gazetteer.osm.OSMTestUtil.osmXmlData;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.gazetteer.common.stream.HoldingConsumer;
import io.gazetteer.osm.model.Entity;
import java.util.Spliterator;
import org.junit.jupiter.api.Test;

public class EntitySpliteratorTest {

  @Test
  public void next() throws Exception {
    Spliterator<Entity> reader = EntityUtil.spliterator(osmXmlData());
    reader.forEachRemaining(fileBlock -> assertNotNull(fileBlock));
    assertFalse(reader.tryAdvance(new HoldingConsumer<>()));
  }

}
