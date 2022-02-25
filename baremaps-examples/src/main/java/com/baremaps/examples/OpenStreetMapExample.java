package com.baremaps.examples;

import com.baremaps.osm.OpenStreetMap;
import com.baremaps.store.AlignedDataList;
import com.baremaps.store.DataStore;
import com.baremaps.store.LongAlignedDataDenseMap;
import com.baremaps.store.LongAlignedDataSortedMap;
import com.baremaps.store.LongDataMap;
import com.baremaps.store.LongDataSortedMap;
import com.baremaps.store.memory.OffHeapMemory;
import com.baremaps.store.type.LonLatDataType;
import com.baremaps.store.type.LongDataType;
import com.baremaps.store.type.LongListDataType;
import com.baremaps.store.type.Pair;
import com.baremaps.store.type.PairDataType;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;

public class OpenStreetMapExample {

  public static void main(String... args) throws IOException {
    String file = args[0];
    InputStream inputStream = new BufferedInputStream(Files.newInputStream(Paths.get(file)));
    Path path = Paths.get(args[0]);
    LongDataMap<Coordinate> nodes = new LongAlignedDataDenseMap(
        new LonLatDataType(),
        new OffHeapMemory());
    LongDataMap<List<Long>> references = new LongDataSortedMap<>(
        new AlignedDataList<>(new PairDataType<>(new LongDataType(), new LongDataType()), new OffHeapMemory()),
        new DataStore<>(new LongListDataType(), new OffHeapMemory()));
    OpenStreetMap.streamPbfBlocksWithGeometries(inputStream, nodes, references, 4386);
  }

}
