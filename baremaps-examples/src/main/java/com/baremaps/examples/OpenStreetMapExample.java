/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.examples;

import com.baremaps.osm.OpenStreetMap;
import com.baremaps.store.AlignedDataList;
import com.baremaps.store.DataStore;
import com.baremaps.store.LongAlignedDataDenseMap;
import com.baremaps.store.LongDataMap;
import com.baremaps.store.LongDataSortedMap;
import com.baremaps.store.memory.OffHeapMemory;
import com.baremaps.store.type.LonLatDataType;
import com.baremaps.store.type.LongDataType;
import com.baremaps.store.type.LongListDataType;
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
    LongDataMap<Coordinate> nodes =
        new LongAlignedDataDenseMap(new LonLatDataType(), new OffHeapMemory());
    LongDataMap<List<Long>> references =
        new LongDataSortedMap<>(
            new AlignedDataList<>(
                new PairDataType<>(new LongDataType(), new LongDataType()), new OffHeapMemory()),
            new DataStore<>(new LongListDataType(), new OffHeapMemory()));
    OpenStreetMap.streamPbfBlocksWithGeometries(inputStream, nodes, references, 4386);
  }
}
