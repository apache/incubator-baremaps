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

package org.apache.baremaps.workflow.tasks;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.apache.baremaps.collection.AppendOnlyBuffer;
import org.apache.baremaps.collection.MemoryAlignedDataList;
import org.apache.baremaps.collection.MonotonicDataMap;
import org.apache.baremaps.collection.memory.MemoryMappedFile;
import org.apache.baremaps.collection.type.*;
import org.apache.baremaps.collection.utils.FileUtils;
import org.apache.baremaps.openstreetmap.model.*;
import org.apache.baremaps.openstreetmap.pbf.PbfEntityReader;
import org.apache.baremaps.openstreetmap.utils.ProjectionTransformer;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public record CreateEntityCollection(Path file, Path collection,
    Integer srid) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportOpenStreetMap.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    logger.info("Importing {} into {}", file, collection);

    var path = file.toAbsolutePath();
    var cacheDir = Files.createTempDirectory(Paths.get("."), "cache_");

    var coordinatesKeysFile = Files.createFile(cacheDir.resolve("coordinates_keys"));
    var coordinatesValsFile = Files.createFile(cacheDir.resolve("coordinates_vals"));
    var coordinateMap = new MonotonicDataMap<>(
        new MemoryAlignedDataList<>(new PairDataType<>(new LongDataType(), new LongDataType()),
            new MemoryMappedFile(coordinatesKeysFile)),
        new AppendOnlyBuffer<>(new LonLatDataType(), new MemoryMappedFile(coordinatesValsFile)));

    var referencesKeysFile = Files.createFile(cacheDir.resolve("references_keys"));
    var referencesValuesFile = Files.createFile(cacheDir.resolve("references_vals"));
    var referenceMap = new MonotonicDataMap<>(
        new MemoryAlignedDataList<>(new PairDataType<>(new LongDataType(), new LongDataType()),
            new MemoryMappedFile(referencesKeysFile)),
        new AppendOnlyBuffer<>(new LongListDataType(), new MemoryMappedFile(referencesValuesFile)));

    Files.deleteIfExists(collection);

    var entityCollection =
        new AppendOnlyBuffer<>(new EntityDataType(), new MemoryMappedFile(collection));

    var projectionTransformer = new ProjectionTransformer(4326, srid);

    // Read the PBF file and dispatch the elements to the consumers
    new PbfEntityReader()
        .geometries(true)
        .coordinateMap(coordinateMap)
        .referenceMap(referenceMap)
        .stream(Files.newInputStream(path))
        .filter(Element.class::isInstance)
        .map(Element.class::cast)
        .filter(element -> !element.getTags().isEmpty())
        .filter(element -> element.getGeometry() != null)
        .map(element -> {
          var geometry = element.getGeometry();
          geometry = projectionTransformer.transform(geometry);
          var tags = new HashMap<String, String>();
          for (var tag : element.getTags().entrySet()) {
            tags.put(tag.getKey(), tag.getValue().toString());
          }
          return new Entity(element.id(), tags, geometry);
        })
        .collect(Collectors.toCollection(() -> entityCollection));

    entityCollection.close();

    FileUtils.deleteRecursively(cacheDir);

    logger.info("Finished importing {} into {}", file, collection);
  }

}
