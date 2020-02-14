package io.gazetteer.osm.osmxml;

import static io.gazetteer.osm.TestUtils.dataOscXml;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.gazetteer.core.stream.AccumulatingConsumer;
import io.gazetteer.core.stream.HoldingConsumer;
import java.util.Spliterator;
import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.Test;

public class ChangeSpliteratorTest {

  @Test
  public void tryAdvance() throws XMLStreamException {
    Spliterator<Change> spliterator = new ChangeSpliterator(dataOscXml());
    spliterator.forEachRemaining(fileBlock -> assertNotNull(fileBlock));
    assertFalse(spliterator.tryAdvance(new HoldingConsumer<>()));
  }

  @Test
  public void forEachRemaining() throws XMLStreamException {
    Spliterator<Change> spliterator = new ChangeSpliterator(dataOscXml());
    AccumulatingConsumer<Change> accumulator = new AccumulatingConsumer<>();
    spliterator.forEachRemaining(accumulator);
    assertEquals(accumulator.values().size(), 51);
  }
}
