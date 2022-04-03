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

package com.baremaps.core;

import com.baremaps.core.blob.BlobStore;
import com.baremaps.core.blob.BlobStoreRouter;
import com.baremaps.core.config.Config;
import com.baremaps.core.database.PostgresBaseTest;
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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class PipelineTest extends PostgresBaseTest {

  @Test
  @Tag("integration")
  void execute() throws IOException, SQLException {
    ObjectMapper mapper = new ObjectMapper();
    URL resource = Resources.getResource("data.json");
    Path directory = Files.createTempDirectory(Paths.get("."), "pipeline_");
    BlobStore blobStore = new BlobStoreRouter();
    DataSource dataSource = initDataSource();
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

          @Override
          public DataSource dataSource() {
            return dataSource;
          }
        };
    Config config = mapper.readValue(resource, Config.class);
    Pipeline pipeline = new Pipeline(context, config);
    pipeline.execute();
    Files.walk(directory).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }
}
