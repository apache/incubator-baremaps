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

package com.baremaps.nic;

import com.google.common.io.Resources;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class NicData {

  private static final String SAMPLE = "sample.txt";

  public static List<NicObject> sample() throws IOException {
    try (InputStream input = Resources.getResource(SAMPLE).openStream()) {
      return NicParser.parse(input).collect(Collectors.toList());
    }
  }
}
