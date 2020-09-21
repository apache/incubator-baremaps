package com.baremaps.osm.reader;

import java.nio.file.Path;

public interface ChangeReader {

  void read(Path path, ChangeHandler handler) throws ReaderException;

}
