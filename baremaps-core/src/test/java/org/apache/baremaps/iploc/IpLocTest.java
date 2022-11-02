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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.baremaps.collection.utils.FileUtils;
import org.apache.baremaps.geocoder.Geocoder;
import org.apache.baremaps.geocoder.geonames.GeonamesGeocoder;
import org.apache.baremaps.iploc.data.InetnumLocation;
import org.apache.baremaps.iploc.data.Ipv4;
import org.apache.baremaps.iploc.data.Ipv4Range;
import org.apache.baremaps.iploc.data.Location;
import org.apache.baremaps.iploc.database.InetnumLocationDao;
import org.apache.baremaps.iploc.database.InetnumLocationDaoSqliteImpl;
import org.apache.baremaps.iploc.database.SqliteUtils;
import org.apache.baremaps.iploc.nic.NicData;
import org.apache.baremaps.iploc.nic.NicObject;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test the IPLoc SQLite database generation using a stream of NIC Objects from a sample NIC txt
 * file and a geocoder from a sample Geonames txt file.
 */
class IpLocTest {

  private static List<NicObject> nicObjects;
  private static IpLoc ipLoc;
  private static InetnumLocationDao inetnumLocationDao;
  private static Path directory;
  private static String databaseUrl;

  @BeforeAll
  public static void beforeAll() throws IOException, URISyntaxException {
    // Load the NIC sample objects
    nicObjects = NicData.sample("ripe/simple_nic_sample.txt");

    // Init the geocoderservice
    directory = Files.createTempDirectory(Paths.get("."), "geocoder_");

    var data = TestFiles.resolve("geonames/geocoder_sample.txt");
    Geocoder geocoder = new GeonamesGeocoder(directory, data);
    geocoder.build();

    // Create the IPLoc service
    databaseUrl = String.format("JDBC:sqlite:%s", directory.resolve("test.db"));
    ipLoc = new IpLoc(databaseUrl, geocoder);

    // Accessor for the database
    inetnumLocationDao = new InetnumLocationDaoSqliteImpl(databaseUrl);
  }

  @AfterAll
  public static void afterAll() throws IOException {
    FileUtils.deleteRecursively(directory);
  }

  @BeforeEach
  public void beforeEach() throws IOException, URISyntaxException, SQLException {
    SqliteUtils.executeResource(databaseUrl, "iploc_init.sql");
  }

  @Test
  void findAll() {
    ipLoc.insertNicObjects(nicObjects.stream());
    List<InetnumLocation> inetnumLocations = inetnumLocationDao.findAll();
    assertEquals(7, inetnumLocations.size());
  }

  @Test
  void findByIpWithZeroes() {
    ipLoc.insertNicObjects(nicObjects.stream());
    List<InetnumLocation> inetnumLocations =
        inetnumLocationDao.findByIp(new Ipv4("0.0.0.5").getIp());
    assertEquals(4, inetnumLocations.size());
  }

  @Test
  void findByIp() {
    ipLoc.insertNicObjects(nicObjects.stream());
    List<InetnumLocation> inetnumLocations =
        inetnumLocationDao.findByIp(new Ipv4("255.22.22.2").getIp());
    assertEquals(1, inetnumLocations.size());
  }

  @Test
  void save() {
    inetnumLocationDao.save(new InetnumLocation("Test", new Ipv4Range("192.168.0.0/24"),
        new Location(1, 1), "Test", null));
    List<InetnumLocation> getAllInetnumLocations = inetnumLocationDao.findAll();
    assertEquals(1, getAllInetnumLocations.size());
  }

  @Test
  void saveMultiple() {
    List<InetnumLocation> inetnumLocations = new ArrayList<>();
    for (int i = 0; i < 30; i++) {
      inetnumLocations.add(new InetnumLocation("Test", new Ipv4Range("192.168.0.0/24"),
          new Location(1, 1), "Test", null));
    }
    inetnumLocationDao.save(inetnumLocations);
    List<InetnumLocation> getAllInetnumLocations = inetnumLocationDao.findAll();
    assertEquals(30, getAllInetnumLocations.size());
  }
}
