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

package com.baremaps.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baremaps.cli.Baremaps;
import com.baremaps.osm.database.NodeTable;
import com.baremaps.util.postgis.PostgisHelper;
import com.google.common.io.CharStreams;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
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

public class OpenStreetMapExampleTest {

  public static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/baremaps?allowMultiQueries=true&user=baremaps&password=baremaps";
  public DataSource dataSource;
  public NodeTable nodeTable;

  @BeforeEach
  public void createTable() throws SQLException, IOException {
    dataSource = PostgisHelper.poolingDataSource(DATABASE_URL);
    nodeTable = new NodeTable(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      PostgisHelper.executeScript(connection, "osm_create_extensions.sql");
      PostgisHelper.executeScript(connection, "osm_drop_tables.sql");
      PostgisHelper.executeScript(connection, "osm_create_tables.sql");
      PostgisHelper.executeScript(connection, "osm_create_primary_keys.sql");
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
        "--input", "openstreetmap/liechtenstein-latest.osm.pbf",
        "--database", DATABASE_URL);
    assertEquals(0, importExitCode);

    // Test the export command
    int exportExitCode = cmd.execute("export",
        "--database", DATABASE_URL,
        "--config", "openstreetmap/config.yaml",
        "--repository", "repository/");
    assertEquals(0, exportExitCode);
    assertTrue(Files.exists(Paths.get("repository/14/8626/5750.pbf")));

    // Test the serve command in a separate thread
    new Thread(() -> {
      cmd.execute("serve",
          "--database", DATABASE_URL,
          "--config", "openstreetmap/config.yaml");
    }).run();

    // Wait for the server to start
    Thread.sleep(1000);

    // Download a static file
    HttpURLConnection indexConnection = (HttpURLConnection) new URL("http://localhost:9000/index.html")
        .openConnection();
    InputStream indexInputStream = indexConnection.getInputStream();
    try (final Reader reader = new InputStreamReader(indexInputStream)) {
      String text = CharStreams.toString(reader);
      assertTrue(text.contains("Baremaps"));
    }
    assertEquals(indexConnection.getResponseCode(), 200);

    // Download a tile file
    HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:9000/tiles/14/8626/5750.pbf")
        .openConnection();
    connection.connect();
    assertEquals(connection.getResponseCode(), 200);
  }
}
