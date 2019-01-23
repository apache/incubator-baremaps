package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.Change;
import io.gazetteer.osm.util.BatchSpliterator;

import java.io.File;
import java.util.Iterator;
import java.util.Spliterator;

import static io.gazetteer.osm.osmxml.XMLUtil.xmlEventReader;

public class ChangeUtil {

    public static Iterator<Change> reader(File file) throws Exception {
        return new ChangeIterator(xmlEventReader(file));
    }

    public static Spliterator<Change> spliterator(File file) throws Exception {
        return new BatchSpliterator<>(reader(file), 10);
    }

}
