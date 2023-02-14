/*
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

package org.apache.baremaps.openstreetmap;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.apache.baremaps.collection.*;
import org.apache.baremaps.collection.memory.MemoryMappedFile;
import org.apache.baremaps.collection.type.LonLatDataType;
import org.apache.baremaps.collection.type.LongDataType;
import org.apache.baremaps.collection.type.LongListDataType;
import org.apache.baremaps.collection.type.PairDataType;
import org.locationtech.jts.geom.Coordinate;

public class CollectionUtils {

  private CollectionUtils() {}

  public static DataMap<Coordinate> denseCoordinateMap() throws IOException {
    var cacheDir = Files.createTempDirectory(Paths.get("."), "coordinates_");
    var coordinatesFile = Files.createFile(cacheDir.resolve("values"));
    var coordinateMap = new MemoryAlignedDataMap<>(
        new LonLatDataType(),
        new MemoryMappedFile(coordinatesFile));
    return coordinateMap;
  }

  public static DataMap<Coordinate> sparseCoordinateMap() throws IOException {
    var cacheDir = Files.createTempDirectory(Paths.get("."), "coordinates_");
    var coordinatesKeysFile = Files.createFile(cacheDir.resolve("keys"));
    var coordinatesValsFile = Files.createFile(cacheDir.resolve("values"));
    var coordinateMap = new MonotonicDataMap<>(
        new MemoryAlignedDataList<>(
            new PairDataType<>(new LongDataType(), new LongDataType()),
            new MemoryMappedFile(coordinatesKeysFile)),
        new AppendOnlyBuffer<>(
            new LonLatDataType(),
            new MemoryMappedFile(coordinatesValsFile)));
    return coordinateMap;
  }

  public static DataMap<List<Long>> sparseReferenceMap() throws IOException {
    var cacheDir = Files.createTempDirectory(Paths.get("."), "references_");
    var referencesKeysFile = Files.createFile(cacheDir.resolve("keys"));
    var referencesValuesFile = Files.createFile(cacheDir.resolve("values"));
    var referenceMap = new MonotonicDataMap<>(
        new MemoryAlignedDataList<>(
            new PairDataType<>(new LongDataType(), new LongDataType()),
            new MemoryMappedFile(referencesKeysFile)),
        new AppendOnlyBuffer<>(
            new LongListDataType(),
            new MemoryMappedFile(referencesValuesFile)));
    return referenceMap;
  }
}
