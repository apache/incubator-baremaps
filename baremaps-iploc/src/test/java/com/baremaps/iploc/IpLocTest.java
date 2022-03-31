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

package com.baremaps.iploc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.geocoder.Geocoder;
import com.baremaps.geocoder.geonames.GeonamesGeocoder;
import com.baremaps.iploc.nic.NicData;
import com.baremaps.iploc.nic.NicObject;
import com.baremaps.iploc.sqlite.SqliteUtils;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test the IPLoc SQLite database generation using a stream of NIC Objects from a sample NIC txt
 * file and a geocoder from a sample Geonames txt file.
 */
class IpLocTest {
  List<NicObject> nicObjects;
  Geocoder geocoder;
  String databaseUrl = "JDBC:sqlite:test.db";
  IpLoc ipLoc;

  @BeforeEach
  public void before() throws IOException, URISyntaxException, SQLException {
    nicObjects = NicData.sample("simple_nic_sample.txt");

    Path path = Files.createTempDirectory(Paths.get("."), "geocoder_");
    URI data = Resources.getResource("geocoder_sample.txt").toURI();
    geocoder = new GeonamesGeocoder(path, data);
    geocoder.build();

    SqliteUtils.executeResource(databaseUrl, "iploc_init.sql");

    ipLoc = new IpLoc(databaseUrl, geocoder);
    ipLoc.insertNicObjects(nicObjects.stream());
  }

  @Test
  void findAll() {
    InetnumLocationDaoImpl inetnumLocationDao = new InetnumLocationDaoImpl(databaseUrl);
    List<InetnumLocation> inetnumLocations = inetnumLocationDao.getAll();
    assertEquals(inetnumLocations.size(), 5);
  }

  @Test
  void findByIpWithZeroes() {
    InetnumLocationDaoImpl inetnumLocationDao = new InetnumLocationDaoImpl(databaseUrl);
    List<InetnumLocation> inetnumLocations =
        inetnumLocationDao.getAllByIp(new Ipv4Range("0.0.0.5/32").start());
    assertEquals(inetnumLocations.size(), 3);
  }

  @Test
  void findByIp() {
    InetnumLocationDaoImpl inetnumLocationDao = new InetnumLocationDaoImpl(databaseUrl);
    List<InetnumLocation> inetnumLocations =
        inetnumLocationDao.getAllByIp(new Ipv4Range("255.22.22.2/32").start());
    assertEquals(inetnumLocations.size(), 1);
  }
}
