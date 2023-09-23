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

import static org.apache.baremaps.stream.ConsumerUtils.consumeThenReturn;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.baremaps.openstreetmap.function.ChangeEntitiesHandler;
import org.apache.baremaps.openstreetmap.function.EntityGeometryBuilder;
import org.apache.baremaps.openstreetmap.function.EntityProjectionTransformer;
import org.apache.baremaps.openstreetmap.postgres.*;
import org.apache.baremaps.openstreetmap.repository.ChangeImporter;
import org.apache.baremaps.openstreetmap.xml.XmlChangeReader;
import org.apache.baremaps.utils.Compression;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ImportOsmChange(Path file, String database, Integer srid,
    Compression compression) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportOsmChange.class);

  public ImportOsmChange(Path file, String database, Integer srid) {
    this(file, database, srid, Compression.detect(file));
  }

  @Override
  public void execute(WorkflowContext context) throws Exception {
    var datasource = context.getDataSource(database);

    var coordinateMap = new PostgresCoordinateMap(datasource);
    var referenceMap = new PostgresReferenceMap(datasource);

    var nodeRepository = new PostgresNodeRepository(datasource);
    var wayRepository = new PostgresWayRepository(datasource);
    var relationRepository = new PostgresRelationRepository(datasource);

    var createGeometry = new EntityGeometryBuilder(coordinateMap, referenceMap);
    var reprojectGeometry = new EntityProjectionTransformer(4326, srid);
    var prepareGeometries = new ChangeEntitiesHandler(createGeometry.andThen(reprojectGeometry));
    var prepareChange = consumeThenReturn(prepareGeometries);
    var importChange = new ChangeImporter(nodeRepository, wayRepository, relationRepository);

    try (var changeInputStream =
        new BufferedInputStream(compression.decompress(Files.newInputStream(file)))) {
      new XmlChangeReader().stream(changeInputStream).map(prepareChange).forEach(importChange);
    }
  }
}
