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

package org.apache.baremaps.rpsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RpslObjectTest {

  private List<RpslObject> objects;

  @BeforeEach
  public void before() throws IOException {
    var file = TestFiles.resolve("baremaps-testing/data/ripe/sample.txt");
    try (var input = Files.newInputStream(file)) {
      objects = new RpslReader().read(input).toList();
    }
  }

  @ParameterizedTest
  @MethodSource("typeProvider")
  void testType(int index, String expectedType) {
    assertEquals(expectedType, objects.get(index).type());
  }

  private static Stream<Arguments> typeProvider() {
    return Stream.of(
        Arguments.of(0, "inetnum"),
        Arguments.of(8, "organisation"),
        Arguments.of(9, "inet6num"));
  }

  @Test
  void id() {
    assertEquals("0.0.0.0 - 0.0.0.255", objects.get(0).id());
    assertEquals("ORG-VDN2-RIPE", objects.get(8).id());
    assertEquals("2001:7fa:0:2::/64", objects.get(9).id());
  }

  @Test
  void attributes() {
    assertEquals(7, objects.get(0).attributes().size());
    assertEquals(5, objects.get(8).attributes().size());
    assertEquals(7, objects.get(9).attributes().size());
  }

  @Test
  void first() {
    RpslObject object = objects.get(0);
    assertEquals("Route de Cheseaux 1", object.first("descr").get());
  }

  @Test
  void all() {
    RpslObject object = objects.get(0);
    assertEquals(3, object.all("descr").size());
  }
}
