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

package org.apache.baremaps.openstreetmap.state;



import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.apache.baremaps.openstreetmap.model.State;

public class StateReader {

  /**
   * Parse an OSM state file.
   *
   * @param input the OpenStreetMap state file
   * @return the state
   */
  public State state(InputStream input) throws IOException {
    InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
    Map<String, String> map = new HashMap<>();
    for (String line : CharStreams.readLines(reader)) {
      String[] array = line.split("=");
      if (array.length == 2) {
        map.put(array[0], array[1]);
      }
    }
    long sequenceNumber = Long.parseLong(map.get("sequenceNumber"));
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    LocalDateTime timestamp = LocalDateTime.parse(map.get("timestamp").replace("\\", ""), format);
    return new State(sequenceNumber, timestamp);
  }
}
