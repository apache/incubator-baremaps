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

package com.baremaps.osm.xml;

import com.baremaps.osm.OsmReader;
import com.baremaps.osm.model.Change;
import java.io.InputStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** A utility class for parsing an OpenStreetMap change file. */
public class XmlChangeReader implements OsmReader<Change> {

  /**
   * Creates an ordered stream of OSM changes from an XML file.
   *
   * @param input
   * @return
   */
  public Stream<Change> stream(InputStream input) {
    return StreamSupport.stream(new XmlChangeSpliterator(input), false);
  }
}
