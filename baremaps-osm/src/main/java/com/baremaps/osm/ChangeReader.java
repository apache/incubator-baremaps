package com.baremaps.osm;

import com.baremaps.osm.domain.Change;
import java.io.IOException;
import java.util.stream.Stream;

public interface ChangeReader {

  Stream<Change> changes() throws IOException;

}
