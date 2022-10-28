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

package org.apache.baremaps.iploc.nic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class NicParserTest {

  @Test
  void parseObjects() throws IOException {
    List<NicObject> nicObjects = NicData.sample("ripe/sample.txt");
    assertEquals(11, nicObjects.size());
  }

  @Test
  void parseAttributes() throws IOException {
    List<NicAttribute> nicAttributes = NicData.sample("ripe/sample.txt").stream()
        .map(NicObject::attributes).flatMap(List::stream).toList();
    assertEquals(233, nicAttributes.size());
  }
}
