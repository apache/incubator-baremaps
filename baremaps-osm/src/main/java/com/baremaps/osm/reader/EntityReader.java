package com.baremaps.osm.reader;

import java.nio.file.Path;

public interface EntityReader {

   void read(Path path, EntityHandler handler) throws ReaderException;

}
