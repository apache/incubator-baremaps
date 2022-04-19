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

package com.baremaps.geocoder.geonames;

import static org.junit.jupiter.api.Assertions.*;

import com.baremaps.geocoder.Geocoder;
import com.baremaps.geocoder.Request;
import com.baremaps.geocoder.Response;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.jupiter.api.Test;

class GeonamesGeocoderTest {

  @Test
  public void buildAndSearch() throws IOException, URISyntaxException, ParseException {
    Path path = Files.createTempDirectory(Paths.get("."), "geocoder_");
    URI data = Resources.getResource("LI.txt").toURI();
    Geocoder geocoder = new GeonamesGeocoder(path, data);
    geocoder.build();

    Response response = geocoder.search(new Request("Bim Alta Schloss", 1));
    assertEquals(1, response.results().size());
    assertEquals("Bim Alta Schloss", response.results().get(0).document().get("name"));

    Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }

  @Test
  public void buildAndSearchWithTheRightCountryCode()
      throws IOException, URISyntaxException, ParseException {
    Path path = Files.createTempDirectory(Paths.get("."), "geocoder_");
    URI data = Resources.getResource("LI.txt").toURI();
    Geocoder geocoder = new GeonamesGeocoder(path, data);
    geocoder.build();

    Response response = geocoder.search(new Request("Bim Alta Schloss", 10, "LI"));
    assertEquals(10, response.results().size());
    assertEquals("Bim Alta Schloss", response.results().get(0).document().get("name"));

    Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }

  @Test
  public void buildAndSearchWithTheWrongCountryCode()
      throws IOException, URISyntaxException, ParseException {
    Path path = Files.createTempDirectory(Paths.get("."), "geocoder_");
    URI data = Resources.getResource("LI.txt").toURI();
    Geocoder geocoder = new GeonamesGeocoder(path, data);
    geocoder.build();

    Response response = geocoder.search(new Request("Bim Alta Schloss", 10, "CH"));
    assertEquals(0, response.results().size());

    Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }
}
