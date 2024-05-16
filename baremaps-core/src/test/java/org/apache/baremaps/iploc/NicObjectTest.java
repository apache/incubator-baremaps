/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.iploc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NicObjectTest {

  private List<NicObject> nicObjects;

  @BeforeEach
  public void before() throws IOException {
    nicObjects = NicData.sample("baremaps-testing/data/ripe/sample.txt");
  }

  @Test
  void type() {
    assertEquals("inetnum", nicObjects.get(0).type());
    assertEquals("organisation", nicObjects.get(8).type());
    assertEquals("inet6num", nicObjects.get(9).type());
  }

  @Test
  void id() {
    assertEquals("0.0.0.0 - 0.0.0.255", nicObjects.get(0).id());
    assertEquals("ORG-VDN2-RIPE", nicObjects.get(8).id());
    assertEquals("2001:7fa:0:2::/64", nicObjects.get(9).id());
  }

  @Test
  void attributes() {
    assertEquals(7, nicObjects.get(0).attributes().size());
    assertEquals(5, nicObjects.get(8).attributes().size());
    assertEquals(7, nicObjects.get(9).attributes().size());
  }

  @Test
  void first() {
    NicObject nicObject = nicObjects.get(0);
    assertEquals("Route de Cheseaux 1", nicObject.first("descr").get());
  }

  @Test
  void all() {
    NicObject nicObject = nicObjects.get(0);
    assertEquals(3, nicObject.all("descr").size());
  }
}
