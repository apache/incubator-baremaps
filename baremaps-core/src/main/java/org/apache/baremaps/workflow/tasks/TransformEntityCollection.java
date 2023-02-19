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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.baremaps.collection.AppendOnlyBuffer;
import org.apache.baremaps.collection.algorithm.UnionStream;
import org.apache.baremaps.collection.memory.MemoryMappedFile;
import org.apache.baremaps.feature.*;
import org.apache.baremaps.mvt.expression.Expressions.Expression;
import org.apache.baremaps.storage.postgres.PostgresDatabase;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record TransformEntityCollection(Path collection, String database,
    Recipe recipe) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(TransformEntityCollection.class);

  record Recipe(String name, Expression<Boolean> filter, List<String> groupBy,
      Operation operation) {
  }

  enum Operation {
    union, merge, none
  }

  @Override
  public void execute(WorkflowContext context) throws Exception {
    logger.info("Transform {} with {}", collection, recipe);

    var featureType = new FeatureType(recipe.name, propertyTypes());

    var groups = new AppendOnlyBuffer<>(new EntityDataType(), new MemoryMappedFile(collection))
        .stream()
        .filter(this::filter)
        .collect(Collectors.groupingBy(this::propertyValues));

    var featureStream = groups.entrySet().stream().flatMap(entry -> {
      var group = entry.getKey();
      var entities = entry.getValue();
      var geometries = simplify(entities.stream().map(Entity::getGeometry));
      return geometries.map(geometry -> new Entity(0, group, geometry));
    });

    var featureSet = new ReadableFeatureStream(featureType, featureStream);

    var dataSource = context.getDataSource(database);
    var postgresDatabase = new PostgresDatabase(dataSource);
    postgresDatabase.write(featureSet);
  }

  private Stream<Geometry> simplify(Stream<Geometry> geometries) {
    return switch (recipe.operation()) {
      case union -> union(geometries);
      case merge -> merge(geometries);
      case none -> geometries;
    };
  }

  private List<String> groupBy() {
    return recipe.groupBy() == null ? List.of() : recipe.groupBy();
  }

  private Map<String, PropertyType> propertyTypes() {
    var map = new HashMap<String, PropertyType>();
    for (var property : groupBy()) {
      map.put(property, new PropertyType<>(property, String.class));
    }
    map.put("geometry", new PropertyType<>("geometry", Geometry.class));
    return map;
  }

  private Map<String, String> propertyValues(Entity entity) {
    var map = new HashMap<String, String>();
    for (var property : groupBy()) {
      map.put(property, entity.getProperty(property).toString());
    }
    return map;
  }

  private Stream<Geometry> union(Stream<Geometry> geometries) {
    var filtered = geometries
        .filter(Polygon.class::isInstance)
        .map(Polygon.class::cast)
        .map(GeometryFixer::fix)
        .filter(Geometry::isValid)
        .collect(Collectors.toCollection(ArrayList::new));
    return new UnionStream(filtered).union();
  }

  private Stream<Geometry> merge(Stream<Geometry> geometries) {
    var filtered = geometries
        .filter(LineString.class::isInstance)
        .map(LineString.class::cast)
        .map(GeometryFixer::fix)
        .toList();

    var lineMerger = new LineMerger();
    lineMerger.add(filtered);
    var mergedGeometries = lineMerger.getMergedLineStrings();

    return mergedGeometries.stream();
  }

  private boolean filter(Entity entity) {
    return recipe.filter.evaluate(entity);
  }

}
