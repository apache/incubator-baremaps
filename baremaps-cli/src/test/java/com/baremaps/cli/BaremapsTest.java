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

package com.baremaps.cli;

import static com.baremaps.testing.TestConstants.DATABASE_URL;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baremaps.collection.utils.FileUtils;
import com.baremaps.core.postgres.PostgresUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class BaremapsTest {

  @BeforeEach
  void init() throws SQLException, IOException {
    DataSource dataSource = PostgresUtils.datasource(DATABASE_URL);
    try (Connection connection = dataSource.getConnection()) {
      PostgresUtils.executeResource(connection, "osm_create_extensions.sql");
      PostgresUtils.executeResource(connection, "osm_drop_tables.sql");
      PostgresUtils.executeResource(connection, "osm_create_tables.sql");
      PostgresUtils.executeResource(connection, "osm_create_gist_indexes.sql");
      PostgresUtils.executeResource(connection, "osm_create_gin_indexes.sql");
    }
  }

  @AfterEach
  void clean() throws IOException {
    Path repository = Paths.get("repository");
    FileUtils.deleteRecursively(repository);
  }

  @Test
  @Tag("integration")
  void test() throws InterruptedException, IOException {
    // Initialize the command line client
    Baremaps app = new Baremaps();
    CommandLine cmd = new CommandLine(app);
    StringWriter writer = new StringWriter();
    cmd.setOut(new PrintWriter(writer));

    // Test the import command
    int importExitCode =
        cmd.execute(
            "import",
            "--database",
            DATABASE_URL,
            "--file",
            "res:///liechtenstein/liechtenstein.osm.pbf");
    assertEquals(0, importExitCode);

    // Test the export command
    int exportExitCode =
        cmd.execute(
            "export",
            "--database",
            DATABASE_URL,
            "--tileset",
            "res:///tileset.json",
            "--repository",
            "repository/");
    assertEquals(0, exportExitCode);
    assertTrue(Files.exists(Paths.get("repository/14/8626/5750.mvt")));

    // Test the serve command in a separate thread
    Thread thread =
        new Thread(
            () -> {
              cmd.execute(
                  "editor",
                  "--database",
                  DATABASE_URL,
                  "--tileset",
                  "res:///tileset.json",
                  "--style",
                  "res:///style.json",
                  "--port",
                  "9000");
            });
    thread.start();

    // Wait for ServiceTalk and JAX-RS to respond with 200 OK
    await()
        .timeout(60, TimeUnit.SECONDS)
        .pollDelay(5, TimeUnit.SECONDS)
        .until(
            () -> {
              HttpURLConnection connection =
                  (HttpURLConnection)
                      new URL("http://127.0.0.1:9000/tiles/14/8626/5750.mvt").openConnection();
              connection.connect();
              return connection.getResponseCode() == 200;
            });

    // test CORS preflight request
    await()
        .timeout(60, TimeUnit.SECONDS)
        .pollDelay(5, TimeUnit.SECONDS)
        .until(
            () -> {
              HttpRequest request =
                  HttpRequest.newBuilder()
                      .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                      .header("Origin", "ThisIsATest")
                      .uri(URI.create("http://127.0.0.1:9000/tiles/14/8626/5750.mvt"))
                      .build();

              HttpResponse<String> response =
                  HttpClient.newBuilder()
                      .build()
                      .send(request, HttpResponse.BodyHandlers.ofString());
              assertEquals(
                  response.headers().firstValue("Access-Control-Allow-Origin").orElse("Nope"),
                  "ThisIsATest");
              return response.statusCode() == 204;
            });
    // test cors
    await()
        .timeout(60, TimeUnit.SECONDS)
        .pollDelay(5, TimeUnit.SECONDS)
        .until(
            () -> {
              HttpRequest request =
                  HttpRequest.newBuilder()
                      .GET()
                      .header("Origin", "ThisIsATest")
                      .uri(URI.create("http://127.0.0.1:9000/tiles/14/8626/5750.mvt"))
                      .build();

              HttpResponse<String> response =
                  HttpClient.newBuilder()
                      .build()
                      .send(request, HttpResponse.BodyHandlers.ofString());
              assertEquals(
                  response.headers().firstValue("Access-Control-Allow-Origin").orElse("Nope"),
                  "ThisIsATest");
              return response.statusCode() == 200;
            });
  }
}
