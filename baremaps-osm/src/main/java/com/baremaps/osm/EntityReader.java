package com.baremaps.osm;

import com.baremaps.osm.domain.Entity;
import java.io.IOException;
import java.util.stream.Stream;

public interface EntityReader {

  Stream<Entity> read() throws IOException;

}
