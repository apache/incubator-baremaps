package io.gazetteer.osm.osmxml;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import java.io.File;
import java.io.FileInputStream;

public class XMLFileUtil {

    public static XMLEventReader reader(File file) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, false);
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
        return factory.createXMLEventReader(new FileInputStream(file));
    }

}
