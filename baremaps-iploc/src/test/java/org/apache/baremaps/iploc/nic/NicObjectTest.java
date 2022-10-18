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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NicObjectTest {

  private List<NicObject> nicObjects;

  @BeforeEach
  public void before() throws IOException {
    nicObjects = NicData.sample("ripe/sample.txt");
  }

  @Test
  void type() {
    assertEquals("organisation", nicObjects.get(0).type());
    assertEquals("mntner", nicObjects.get(1).type());
    assertEquals("organisation", nicObjects.get(2).type());
    assertEquals("inetnum", nicObjects.get(3).type());
  }

  @Test
  void id() {
    assertEquals("ORG-HEDI1-RIPE", nicObjects.get(0).id());
    assertEquals("ch-heig-vd-1-mnt", nicObjects.get(1).id());
    assertEquals("ORG-VA29373-RIPE", nicObjects.get(2).id());
    assertEquals("193.135.138.0 - 193.135.138.255", nicObjects.get(3).id());
  }

  @Test
  void attributes() {
    assertEquals(21, nicObjects.get(0).attributes().size());
    assertEquals(15, nicObjects.get(1).attributes().size());
    assertEquals(18, nicObjects.get(2).attributes().size());
    assertEquals(23, nicObjects.get(3).attributes().size());
  }

  @Test
  void first() {
    NicObject nicObject = nicObjects.get(0);
    assertEquals("RIPE-NCC-HM-MNT", nicObject.first("mnt-by").get());
    assertEquals("****************************", nicObject.first("remarks").get());
  }

  @Test
  void all() {
    NicObject nicObject = nicObjects.get(0);
    assertEquals(2, nicObject.all("mnt-by").size());
    assertEquals(7, nicObject.all("remarks").size());
  }
}
