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

package org.apache.baremaps.database.database;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.baremaps.database.BlockImporter;
import org.apache.baremaps.database.PostgresUtils;
import org.apache.baremaps.database.repository.PostgresHeaderRepository;
import org.apache.baremaps.database.repository.PostgresNodeRepository;
import org.apache.baremaps.database.repository.PostgresRelationRepository;
import org.apache.baremaps.database.repository.PostgresWayRepository;
import org.apache.baremaps.database.repository.RepositoryException;
import org.apache.baremaps.openstreetmap.pbf.PbfBlockReader;
import org.apache.baremaps.testing.PostgresContainerTest;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class BlockImporterTest extends PostgresContainerTest {

  public DataSource dataSource;
  public PostgresHeaderRepository headerRepository;
  public PostgresNodeRepository nodeRepository;
  public PostgresWayRepository tableRepository;
  public PostgresRelationRepository relationRepository;

  @BeforeEach
  void init() throws SQLException, IOException {
    dataSource = PostgresUtils.dataSource(jdbcUrl(), 1);
    headerRepository = new PostgresHeaderRepository(dataSource);
    nodeRepository = new PostgresNodeRepository(dataSource);
    tableRepository = new PostgresWayRepository(dataSource);
    relationRepository = new PostgresRelationRepository(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      PostgresUtils.executeResource(connection, "queries/osm_create_extensions.sql");
      PostgresUtils.executeResource(connection, "queries/osm_drop_tables.sql");
      PostgresUtils.executeResource(connection, "queries/osm_create_tables.sql");
    }
  }

  @Test
  @Tag("integration")
  void test() throws RepositoryException, URISyntaxException, IOException {
    // Import data
    BlockImporter blockImporter =
        new BlockImporter(headerRepository, nodeRepository, tableRepository, relationRepository);

    try (InputStream inputStream = Files.newInputStream(TestFiles.resolve("simple/data.osm.pbf"))) {

      new PbfBlockReader().stream(inputStream).forEach(blockImporter);

      // Check node importation
      assertNull(nodeRepository.get(0l));
      assertNotNull(nodeRepository.get(1l));
      assertNotNull(nodeRepository.get(2l));
      assertNotNull(nodeRepository.get(3l));
      assertNull(nodeRepository.get(4l));

      // Check way importation
      assertNull(tableRepository.get(0l));
      assertNotNull(tableRepository.get(1l));
      assertNull(tableRepository.get(2l));

      // Check relation importation
      assertNull(relationRepository.get(0l));
      assertNotNull(relationRepository.get(1l));
      assertNull(relationRepository.get(2l));
    }
  }
}
