package com.baremaps.osm;

import com.baremaps.osm.model.Entity;
import java.io.IOException;
import java.util.stream.Stream;

public interface EntityReader {

  Stream<Entity> entities() throws IOException;

}
