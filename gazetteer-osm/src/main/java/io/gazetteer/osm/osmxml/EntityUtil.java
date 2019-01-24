package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.Entity;
import io.gazetteer.osm.util.BatchSpliterator;

import java.io.File;
import java.util.Iterator;
import java.util.Spliterator;

import static io.gazetteer.osm.osmxml.XMLUtil.xmlEventReader;

public class EntityUtil {

    public static Iterator<Entity> iterator(File file) throws Exception {
        return new EntityIterator(xmlEventReader(file));
    }

    public static Spliterator<Entity> spliterator(File file) throws Exception {
        return new BatchSpliterator<>(iterator(file), 10);
    }

}
