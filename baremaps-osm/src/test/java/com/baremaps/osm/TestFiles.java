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

import java.net.URI;
import java.net.URISyntaxException;

public class TestFiles {

  public static URI dataOsmPbf() throws URISyntaxException {
    return TestFiles.class.getClassLoader().getResource("data.osm.pbf").toURI();
  }

  public static URI dataOsmXml() throws URISyntaxException {
    return TestFiles.class.getClassLoader().getResource("data.osm.xml").toURI();
  }

  public static URI updateOsmPbf() throws URISyntaxException {
    return TestFiles.class.getClassLoader().getResource("update.osm.pbf").toURI();
  }

  public static URI dataOscXml() throws URISyntaxException {
    return TestFiles.class.getClassLoader().getResource("data.osc.xml").toURI();
  }

  public static URI dataStateTxt() throws URISyntaxException {
    return TestFiles.class.getClassLoader().getResource("data-state.txt").toURI();
  }
}
