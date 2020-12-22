package com.baremaps.osm;

import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.pbf.Block;
import java.io.IOException;
import java.util.stream.Stream;

public interface BlockReader {

  Stream<Block> blocks() throws IOException;

}
