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

package com.baremaps.pipeline;

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.BlobStoreRouter;
import com.baremaps.blob.HttpBlobStore;
import com.baremaps.pipeline.config.Config;
import com.baremaps.pipeline.config.Database;
import com.baremaps.pipeline.database.PostgresBaseTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Comparator;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

class PipelineTest extends PostgresBaseTest {

  PostgreSQLContainer container;

  @BeforeEach
  public void before() {
    DockerImageName postgis =
        DockerImageName.parse("postgis/postgis:13-3.1").asCompatibleSubstituteFor("postgres");
    container = new PostgreSQLContainer(postgis);
    container.start();
  }

  @AfterEach
  public void after() {
    container.stop();
  }

  @Test
  @Disabled
  void execute() throws IOException, SQLException {
    ObjectMapper mapper = new ObjectMapper();
    URL resource = Resources.getResource("config.json");
    Path directory = Files.createTempDirectory(Paths.get("."), "pipeline_");
    BlobStore blobStore =
        new BlobStoreRouter()
            .addScheme("http", new HttpBlobStore())
            .addScheme("https", new HttpBlobStore());
    Context context =
        new Context() {
          @Override
          public Path directory() {
            return directory;
          }

          @Override
          public BlobStore blobStore() {
            return blobStore;
          }

        };
    Config config = mapper.readValue(resource, Config.class);
    Database database = new Database();
    database.setHost(container.getHost());
    database.setName(container.getDatabaseName());
    database.setUsername(container.getUsername());
    database.setPassword(container.getPassword());
    database.setSchema("public");
    database.setPort(container.getMappedPort(5432));
    config.setDatabase(database);

    Pipeline pipeline = new Pipeline(context, config);
    pipeline.execute();
    Files.walk(directory).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }
}
