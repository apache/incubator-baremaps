package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.model.Change;
import io.gazetteer.osm.util.BatchSpliterator;

import java.io.File;
import java.util.Iterator;
import java.util.Spliterator;

import static io.gazetteer.osm.osmxml.XMLUtil.xmlEventReader;

public class ChangeUtil {

  public static Iterator<Change> iterator(File file) throws Exception {
    return new ChangeIterator(xmlEventReader(file));
  }

  public static Spliterator<Change> spliterator(File file) throws Exception {
    return new BatchSpliterator<>(iterator(file), 10);
  }
}
