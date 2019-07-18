package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.model.Entity;
import io.gazetteer.common.stream.HoldingConsumer;
import java.util.Spliterator;
import org.junit.jupiter.api.Test;

import static io.gazetteer.osm.OSMTestUtil.osmXmlData;
import static org.junit.jupiter.api.Assertions.*;

public class EntitySpliteratorTest {

  @Test
  public void next() throws Exception {
    Spliterator<Entity> reader = EntityUtil.spliterator(osmXmlData());
    reader.forEachRemaining(fileBlock -> assertNotNull(fileBlock));
    assertFalse(reader.tryAdvance(new HoldingConsumer<>()));
  }

}
