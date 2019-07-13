package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.model.Change;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static io.gazetteer.osm.OSMTestUtil.osmPbfData;
import static org.junit.jupiter.api.Assertions.*;

public class ChangeIteratorTest {

  /*
  @Test
  public void next() throws Exception {
    Iterator<Change> reader = ChangeUtil.iterator(osmPbfData());
    while (reader.hasNext()) {
      Change block = reader.next();
      assertNotNull(block);
    }
    assertFalse(reader.hasNext());
  }

  @Test
  public void nextException() {
    assertThrows(NoSuchElementException.class, () -> {
      Iterator<Change> reader = ChangeUtil.iterator(osmPbfData());
      while (reader.hasNext()) {
        reader.next();
      }
      reader.next();
    });
  }
  */
}
