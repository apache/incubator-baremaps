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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baremaps.osm.postgres.PostgresNodeTable;
import com.baremaps.postgres.jdbc.PostgresUtils;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class BaremapsTest {

  public static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/baremaps?user=baremaps&password=baremaps";
  public DataSource dataSource;
  public PostgresNodeTable nodeStore;

  @BeforeEach
  public void createTable() throws SQLException, IOException {
    dataSource = PostgresUtils.datasource(DATABASE_URL);
    nodeStore = new PostgresNodeTable(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      PostgresUtils.executeResource(connection, "osm_create_extensions.sql");
      PostgresUtils.executeResource(connection, "osm_drop_tables.sql");
      PostgresUtils.executeResource(connection, "osm_create_tables.sql");
      PostgresUtils.executeResource(connection, "osm_create_gist_indexes.sql");
      PostgresUtils.executeResource(connection, "osm_create_gin_indexes.sql");
    }
  }

  @AfterEach
  public void clean() throws IOException {
    Path repository = Paths.get("repository");
    Files.walk(repository)
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
  }

  @Test
  @Tag("integration")
  public void test() throws InterruptedException, IOException {
    // Initialize the command line client
    Baremaps app = new Baremaps();
    CommandLine cmd = new CommandLine(app);
    StringWriter writer = new StringWriter();
    cmd.setOut(new PrintWriter(writer));

    // Test the import command
    int importExitCode = cmd.execute("import",
        "--database", DATABASE_URL,
        "--file", "res://liechtenstein/liechtenstein.osm.pbf");
    assertEquals(0, importExitCode);

    // Test the export command
    int exportExitCode = cmd.execute("export",
        "--database", DATABASE_URL,
        "--tileset", "res://tileset.json",
        "--repository", "repository/");
    assertEquals(0, exportExitCode);
    assertTrue(Files.exists(Paths.get("repository/14/8626/5750.pbf")));

    // Test the serve command in a separate thread
    Thread thread = new Thread(() -> {
      cmd.execute("edit",
          "--database", DATABASE_URL,
          "--tileset", "res://tileset.json",
          "--style", "res://style.json",
          "--port", "9000");
    });
    thread.start();

    // Wait for ServiceTalk and JAX-RS  to start
    Thread.sleep(5000);

    // Download a tile file
    HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:9000/tiles/14/8626/5750.mvt")
        .openConnection();
    connection.connect();
    assertEquals(connection.getResponseCode(), 200);
  }
}
