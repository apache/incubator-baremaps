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
import org.apache.baremaps.rpsl.RpslObject.RpslAttribute;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Test;

class RpslParserTest {

  @Test
  void parseObjects() throws IOException {
    var file = TestFiles.resolve("baremaps-testing/data/ripe/sample.txt");
    try (var input = Files.newInputStream(file)) {
      List<RpslObject> objects = new RpslReader().read(input).toList();
      assertEquals(10, objects.size());
    }
  }

  @Test
  void parseAttributes() throws IOException {
    var file = TestFiles.resolve("baremaps-testing/data/ripe/sample.txt");
    try (var input = Files.newInputStream(file)) {
      List<RpslObject> objects = new RpslReader().read(input).toList();
      List<RpslAttribute> attributes = objects.stream()
          .map(RpslObject::attributes).flatMap(List::stream).toList();
      assertEquals(58, attributes.size());
    }
  }
}
