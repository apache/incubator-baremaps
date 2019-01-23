package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.Change;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.EOFException;
import java.io.IOException;
import java.text.ParseException;

import static io.gazetteer.osm.osmxml.EntityReader.*;

public class ChangeReader {

    private final XMLEventReader reader;

    private Change.Type type;

    public ChangeReader(XMLEventReader reader) {
        this.reader = reader;
    }

    public Change read() throws IOException, XMLStreamException, ParseException {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (isElement(event, Change.Type.create.name())) {
                type = Change.Type.create;
            } else if (isElement(event, Change.Type.modify.name())) {
                type = Change.Type.modify;
            } else if (isElement(event, Change.Type.delete.name())) {
                type = Change.Type.delete;
            } else if (isElement(event, NODE)) {
                return new Change(type, readNode(event.asStartElement(), reader));
            } else if (isElement(event, WAY)) {
                return new Change(type, readWay(event.asStartElement(), reader));
            } else if (isElement(event, RELATION)) {
                return new Change(type, readRelation(event.asStartElement(), reader));
            }
        }
        throw new EOFException();
    }


}
