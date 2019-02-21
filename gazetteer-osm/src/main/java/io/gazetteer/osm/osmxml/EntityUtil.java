package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.model.Entity;
import io.gazetteer.osm.util.BatchSpliterator;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Spliterator;

import static io.gazetteer.osm.osmxml.XMLUtil.xmlEventReader;

public class EntityUtil {

  public static Iterator<Entity> iterator(InputStream input) throws Exception {
    return new EntityIterator(xmlEventReader(input));
  }

  public static Spliterator<Entity> spliterator(InputStream input) throws Exception {
    return new BatchSpliterator<>(iterator(input), 10);
  }
}
