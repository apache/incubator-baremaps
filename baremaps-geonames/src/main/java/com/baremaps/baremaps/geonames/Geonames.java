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

package com.baremaps.baremaps.geonames;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** Utils for Geonames data */
public class Geonames {
  public static Stream<GeonamesRecord> parse(InputStream inputStream) throws IOException {
    return StreamSupport.stream(new GeonamesSpliterator(inputStream), false);
  }
}
