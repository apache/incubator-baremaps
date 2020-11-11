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

package com.baremaps.osm;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestFiles {

  public static InputStream dataOsmPbf() {
    return TestFiles.class.getClassLoader().getResourceAsStream("data.osm.pbf");
  }

  public static InputStream denseNodesOsmPbf() {
    return TestFiles.class.getClassLoader().getResourceAsStream("dense.osm.pbf");
  }

  public static InputStream waysOsmPbf() {
    return TestFiles.class.getClassLoader().getResourceAsStream("ways.osm.pbf");
  }

  public static InputStream relationsOsmPbf() {
    return TestFiles.class.getClassLoader().getResourceAsStream("relations.osm.pbf");
  }

  public static InputStream dataOsmXml() {
    return TestFiles.class.getClassLoader().getResourceAsStream("data.osm.xml");
  }

  public static InputStream dataOscXml() {
    return TestFiles.class.getClassLoader().getResourceAsStream("data.osc.xml");
  }

  public static Path monacoOsmPbf() throws URISyntaxException {
      return Paths.get(TestFiles.class.getClassLoader().getResource("monaco.osm.pbf").toURI());
  }

  public static Path monacoOsmBz2() throws URISyntaxException {
      return Paths.get(TestFiles.class.getClassLoader().getResource("monaco.osm.bz2").toURI());
  }

  public static Path monocoStateTxt() throws URISyntaxException {
      return Paths.get(TestFiles.class.getClassLoader().getResource("monaco-state.txt").toURI());
  }

}
