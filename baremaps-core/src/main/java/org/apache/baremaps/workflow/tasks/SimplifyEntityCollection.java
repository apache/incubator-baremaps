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


import java.nio.file.Path;
import java.util.Map;
import java.util.function.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.baremaps.collection.*;
import org.apache.baremaps.collection.AppendOnlyBuffer;
import org.apache.baremaps.collection.memory.MemoryMappedFile;
import org.apache.baremaps.collection.type.*;
import org.apache.baremaps.feature.*;
import org.apache.baremaps.mvt.expression.Expressions.Expression;
import org.apache.baremaps.storage.postgres.PostgresDatabase;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record SimplifyEntityCollection(Path collection, String database,
    Recipe recipe) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportOpenStreetMap.class);

  record Recipe(String name, Expression<Boolean> filter, Operation operation) {
  }

  enum Operation {
    union, merge, none
  }

  @Override
  public void execute(WorkflowContext context) throws Exception {
    // read the original collection
    var entities =
        new AppendOnlyBuffer<>(new EntityDataType(), new MemoryMappedFile(collection));

    // filter the entities of the collection
    var filteredEntities = entities.stream()
        .filter(entity -> recipe.filter.evaluate(entity))
        .toList();

    var geometries = filteredEntities.stream()
        .map(Entity::getGeometry)
        .filter(Polygon.class::isInstance)
        .map(Polygon.class::cast)
        .map(GeometryFixer::fix)
        .toList();

    // Apply the operation to each recipe and store the result in the collection
    var unionedGeometry = new CascadedPolygonUnion(geometries).union();

    var featureType =
        new FeatureType(recipe.name, Map.of("geom", new PropertyType("geom", Geometry.class)));

    Stream<Feature> stream = IntStream.range(0, unionedGeometry.getNumGeometries())
        .mapToObj(unionedGeometry::getGeometryN)
        .map(geometry -> new FeatureImpl(featureType, Map.of("geom", geometry)));

    var dataSource = context.getDataSource(database);
    var postgresDatabase = new PostgresDatabase(dataSource);
    postgresDatabase.write(new ReadableFeatureSet() {
      @Override
      public FeatureType getType() {
        return featureType;
      }

      @Override
      public Stream<Feature> read() {
        return stream;
      }
    });
  }
}
