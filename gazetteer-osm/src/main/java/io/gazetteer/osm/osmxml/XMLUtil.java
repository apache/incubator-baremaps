package io.gazetteer.osm.osmxml;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import java.io.File;
import java.io.FileInputStream;

public class XMLUtil {

    public static XMLEventReader xmlEventReader(File file) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, false);
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
        return factory.createXMLEventReader(new FileInputStream(file));
    }

    public static EntityReader reader(File file) throws Exception {
        return new EntityReader(xmlEventReader(file));
    }

    public static EntitySpliterator spliterator(File file) throws Exception {
        return new EntitySpliterator(reader(file));
    }

}
