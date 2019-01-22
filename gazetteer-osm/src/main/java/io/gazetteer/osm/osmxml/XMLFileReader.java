package io.gazetteer.osm.osmxml;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import java.io.FileInputStream;

public class FileReader {


    static public void main(String[] args) throws Exception {
        String fileName = "/home/bchapuis/Datasets/osm/switzerland-latest.osm.bz2";
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(fileName));

    }

}
