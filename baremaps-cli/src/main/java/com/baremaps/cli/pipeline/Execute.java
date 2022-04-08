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

package com.baremaps.cli.pipeline;

import static com.baremaps.core.tile.VariableUtils.interpolate;

import com.baremaps.blob.BlobStore;
import com.baremaps.cli.Options;
import com.baremaps.core.postgres.PostgresUtils;
import com.baremaps.stream.StreamException;
import com.baremaps.stream.StreamUtils;
import com.google.common.base.Splitter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "execute", description = "Execute queries in the database.")
public class Execute implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Execute.class);

  @Mixin private Options options;

  @Option(
      names = {"--database"},
      paramLabel = "DATABASE",
      description = "The JDBC url of the database.",
      required = true)
  private String database;

  @Option(
      names = {"--file"},
      paramLabel = "FILE",
      description = "The SQL file to execute in the database.",
      required = true)
  private List<URI> files;

  @Option(
      names = {"--parallel"},
      paramLabel = "PARALLEL",
      negatable = true,
      description = "Enable parallel execution of queries.")
  public boolean parallel = true;

  @Override
  public Integer call() throws Exception {
    DataSource datasource = PostgresUtils.datasource(database);
    BlobStore blobStore = options.blobStore();

    for (URI file : files) {
      logger.info("Execute {}", file);
      String blob =
          new String(blobStore.get(file).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      blob = interpolate(System.getenv(), blob);
      StreamUtils.batch(Splitter.on(";").splitToStream(blob), 1)
          .forEach(
              query -> {
                try (Connection connection = datasource.getConnection();
                    Statement statement = connection.createStatement()) {
                  statement.execute(query);
                } catch (SQLException e) {
                  throw new StreamException(e);
                }
              });
    }

    logger.info("Done");

    return 0;
  }
}
