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

package org.apache.baremaps.geocoder.geonames;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import org.apache.baremaps.geocoder.Request;
import org.apache.baremaps.testing.TestFiles;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.jupiter.api.Test;

class GeonamesGeocoderTest {

  @Test
  public void buildAndSearch() throws IOException, URISyntaxException, ParseException {
    var path = Files.createTempDirectory(Paths.get("."), "geocoder_");
    var data = TestFiles.resolve("geonames/LI.txt");
    var geocoder = new GeonamesGeocoder(path, data);
    geocoder.build();

    var response = geocoder.search(new Request("Bim Alta Schloss", 1));
    assertEquals(1, response.results().size());
    assertEquals("Bim Alta Schloss", response.results().get(0).document().get("name"));

    Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }

  @Test
  public void buildAndSearchWithTheRightCountryCode()
      throws IOException, URISyntaxException, ParseException {
    var path = Files.createTempDirectory(Paths.get("."), "geocoder_");
    var data = TestFiles.resolve("geonames/LI.txt");
    var geocoder = new GeonamesGeocoder(path, data);
    geocoder.build();

    var response = geocoder.search(new Request("Bim Alta Schloss", 10, "LI"));
    assertEquals(10, response.results().size());
    assertEquals("Bim Alta Schloss", response.results().get(0).document().get("name"));

    Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }

  @Test
  public void buildAndSearchWithTheWrongCountryCode()
      throws IOException, URISyntaxException, ParseException {
    var path = Files.createTempDirectory(Paths.get("."), "geocoder_");
    var data = TestFiles.resolve("geonames/LI.txt");
    var geocoder = new GeonamesGeocoder(path, data);
    geocoder.build();

    var response = geocoder.search(new Request("Bim Alta Schloss", 10, "CH"));
    assertEquals(0, response.results().size());

    Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }
}
