package com.baremaps.osm;

import com.baremaps.osm.model.Change;
import java.io.IOException;
import java.util.stream.Stream;

public interface ChangeReader {

  Stream<Change> read() throws IOException;

}
