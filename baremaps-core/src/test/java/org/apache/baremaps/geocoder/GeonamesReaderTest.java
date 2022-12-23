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

package org.apache.baremaps.geocoder;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Test;

class GeonamesReaderTest {

  @Test
  void read() throws IOException {
    var data = TestFiles.resolve("geonames/geocoder_sample.txt");
    try (var inputStream = Files.newInputStream(data)) {
      var reader = new GeonamesReader();
      var stream = reader.stream(inputStream);

      var list = stream.collect(Collectors.toList());
      assertEquals(4, list.size());

      var record = list.get(0);
      assertEquals(1, record.getGeonameid());
      assertEquals("HEIG", record.getAsciiname());
    }
  }
}
