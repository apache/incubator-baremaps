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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.baremaps.collection.*;
import org.apache.baremaps.collection.memory.MappedMemory;
import org.apache.baremaps.collection.memory.OffHeapMemory;
import org.apache.baremaps.collection.store.AppendOnlyStore;
import org.apache.baremaps.collection.store.MemoryAlignedDataStore;
import org.apache.baremaps.collection.type.*;
import org.apache.baremaps.openstreetmap.model.Element;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record SimplifyOpenStreetMap(Path file, String database, Integer databaseSrid) implements Task {

    private static final Logger logger = LoggerFactory.getLogger(ImportOpenStreetMap.class);

    @Override
    public void execute(WorkflowContext context) throws Exception {
        logger.info("Importing {} into {}", file, database);

        var path = file.toAbsolutePath();

        var cacheDir = Files.createTempDirectory(Paths.get("."), "cache_");

        var coordinatesKeysFile = Files.createFile(cacheDir.resolve("coordinates_keys"));
        var coordinatesValsFile = Files.createFile(cacheDir.resolve("coordinates_vals"));
        var coordinateMap =
                new SortedLongVariableSizeDataMap<>(
                        new MemoryAlignedDataStore<>(
                                new PairDataType<>(
                                        new LongDataType(),
                                        new LongDataType()
                                ), new MappedMemory(coordinatesKeysFile)),
                        new AppendOnlyStore<>(
                                new LonLatDataType(),
                                new MappedMemory(coordinatesValsFile)));

        var referencesKeysFile = Files.createFile(cacheDir.resolve("references_keys"));
        var referencesValuesFile = Files.createFile(cacheDir.resolve("references_vals"));
        var referenceMap =
                new SortedLongVariableSizeDataMap<>(
                        new MemoryAlignedDataStore<>(
                                new PairDataType<>(
                                        new LongDataType(),
                                        new LongDataType()
                                ), new MappedMemory(referencesKeysFile)),
                        new AppendOnlyStore<>(
                                new LongListDataType(),
                                new MappedMemory(referencesValuesFile)));

        var collection = new IndexedLongMap<>(
                new LongLongMap(new OffHeapMemory()),
                new AppendOnlyStore<>(new GeometryDataType(), new OffHeapMemory()));


//        new PbfEntityReader(
//                new PbfBlockReader()
//                        .geometries(true)
//                        .coordinateMap(coordinateMap)
//                        .referenceMap(referenceMap))
//                .stream(Files.newInputStream(path))
//                .filter(Element.class::isInstance)
//                .map(Element.class::cast)
//                .filter(element -> element.getTags().containsKey("building"))
//                .map(Element::getGeometry)
//                .filter(Predicates.notNull())
//                .forEach(collection::put);
//
//        var unionedGeometry = new CascadedPolygonUnion(new CollectionAdapter(collection)).union();
//
//        var unionGeometries = IntStream.range(0, unionedGeometry.getNumGeometries())
//                .mapToObj(unionedGeometry::getGeometryN)
//                .toList();
//
//        System.out.println(unionGeometries.size());
//
//        FileUtils.deleteRecursively(cacheDir);
//
//        logger.info("Finished importing {} into {}", file, database);
    }

    public static class PolygonUnionConsumer implements Consumer<Element> {

        private final Map<Map<String, Object>, Collection<Element>> groups = new ConcurrentHashMap<>();

        private final Supplier<Collection<Element>> collectionSupplier;

        private final Predicate<Element> filter;

        private final List<String> groupBy;

        public PolygonUnionConsumer(Supplier<Collection<Element>> collectionSupplier, Predicate<Element> filter, List<String> groupBy) {
            this.collectionSupplier = collectionSupplier;
            this.filter = filter;
            this.groupBy = groupBy;
        }

        @Override
        public void accept(Element element) {
            if (filter.test(element)) {
                var key = groupBy.stream().collect(Collectors.toMap(Function.identity(), element.getTags()::get));
                var collection = groups.computeIfAbsent(key, k -> collectionSupplier.get());
                collection.add(element);
            }
        }

        public Stream<Geometry> geometries() {
            return groups.entrySet().stream()
                    .flatMap(entry -> {
                        //var tags = entry.getKey();
                        var collection = entry.getValue();
                        var geometry = new CascadedPolygonUnion(collection).union();
                        return IntStream.range(0, geometry.getNumGeometries())
                                .mapToObj(geometry::getGeometryN);
                    });
        }
    }}
