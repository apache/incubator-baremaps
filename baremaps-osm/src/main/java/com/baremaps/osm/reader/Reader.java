package com.baremaps.osm.reader;

import java.nio.file.Path;

public interface Reader<Handler> {

  void read(Path path, Handler handler) throws ReaderException;

}
