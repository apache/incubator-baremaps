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

package org.apache.baremaps.iploc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.baremaps.iploc.data.IpLoc;
import org.apache.baremaps.iploc.data.Ipv4;
import org.apache.baremaps.iploc.data.Ipv4Range;
import org.apache.baremaps.iploc.data.Location;
import org.apache.baremaps.iploc.nic.NicData;
import org.apache.baremaps.iploc.nic.NicObject;
import org.apache.baremaps.testing.TestFiles;
import org.apache.baremaps.utils.FileUtils;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.tasks.CreateGeonamesIndex;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

/**
 * Test the IPLoc SQLite database generation using a stream of NIC Objects from a sample NIC txt
 * file and a geocoder from a sample Geonames txt file.
 */
class IpLocBuilderTest {

  private static List<NicObject> nicObjects;
  private static IpLocManager ipLocBuilder;
  private static IpLocRepository iplocRepository;
  private static Path directory;
  private static String jdbcUrl;

  @BeforeAll
  public static void beforeAll() throws Exception {
    // Load the NIC sample objects
    nicObjects = NicData.sample("ripe/simple_nic_sample.txt");

    // Init the geocoderservice
    directory = Files.createTempDirectory(Paths.get("."), "geocoder_");

    // Create the geonames index
    var data = TestFiles.resolve("geonames/geocoder_sample.txt");
    var task = new CreateGeonamesIndex(data, directory);
    task.execute(new WorkflowContext());

    // Create the IPLoc service
    jdbcUrl = String.format("JDBC:sqlite:%s", directory.resolve("test.db"));
    var config = new SQLiteConfig();
    var dataSource = new SQLiteDataSource(config);
    dataSource.setUrl(jdbcUrl);

    var dir = MMapDirectory.open(directory);
    var searcherManager = new SearcherManager(dir, new SearcherFactory());

    iplocRepository = new IpLocRepository(dataSource);
    ipLocBuilder = new IpLocManager(iplocRepository, searcherManager);
  }

  @AfterAll
  public static void afterAll() throws IOException {
    FileUtils.deleteRecursively(directory);
  }

  @BeforeEach
  public void beforeEach() throws IOException, SQLException {
    iplocRepository.dropTable();
    iplocRepository.createTable();
    iplocRepository.createIndex();
  }

  @Test
  void findAll() {
    ipLocBuilder.insertNicObjects(nicObjects.stream());
    List<IpLoc> inetnumLocations = iplocRepository.findAll();
    assertEquals(7, inetnumLocations.size());
  }

  @Test
  void findByIpWithZeroes() {
    ipLocBuilder.insertNicObjects(nicObjects.stream());
    List<IpLoc> inetnumLocations =
        iplocRepository.findByIp(new Ipv4("0.0.0.5").getIp());
    assertEquals(4, inetnumLocations.size());
  }

  @Test
  void findByIp() {
    ipLocBuilder.insertNicObjects(nicObjects.stream());
    List<IpLoc> inetnumLocations =
        iplocRepository.findByIp(new Ipv4("255.22.22.2").getIp());
    assertEquals(1, inetnumLocations.size());
  }

  @Test
  void save() {
    iplocRepository.save(new IpLoc("Test", new Ipv4Range("192.168.0.0/24"),
        new Location(1, 1), "Test", null));
    List<IpLoc> getAllInetnumLocations = iplocRepository.findAll();
    assertEquals(1, getAllInetnumLocations.size());
  }

  @Test
  void saveMultiple() {
    List<IpLoc> inetnumLocations = new ArrayList<>();
    for (int i = 0; i < 30; i++) {
      inetnumLocations.add(new IpLoc("Test", new Ipv4Range("192.168.0.0/24"),
          new Location(1, 1), "Test", null));
    }
    iplocRepository.save(inetnumLocations);
    List<IpLoc> getAllInetnumLocations = iplocRepository.findAll();
    assertEquals(30, getAllInetnumLocations.size());
  }
}
