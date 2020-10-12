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

package com.baremaps.osm.reader;

import java.io.InputStream;

public class DataFiles {

  public static InputStream dataOsmPbf() {
    return DataFiles.class.getClassLoader().getResourceAsStream("data.osm.pbf");
  }

  public static InputStream denseOsmPbf() {
    return DataFiles.class.getClassLoader().getResourceAsStream("dense.osm.pbf");
  }

  public static InputStream waysOsmPbf() {
    return DataFiles.class.getClassLoader().getResourceAsStream("ways.osm.pbf");
  }

  public static InputStream relationsOsmPbf() {
    return DataFiles.class.getClassLoader().getResourceAsStream("relations.osm.pbf");
  }

  public static InputStream dataOsmXml() {
    return DataFiles.class.getClassLoader().getResourceAsStream("data.osm.xml");
  }

  public static InputStream dataOscXml() {
    return DataFiles.class.getClassLoader().getResourceAsStream("data.osc.xml");
  }

}
