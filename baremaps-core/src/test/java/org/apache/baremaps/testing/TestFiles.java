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

package org.apache.baremaps.testing;



import java.nio.file.Path;

public class TestFiles {

  public static final Path DATA_OSM_PBF = resolve("data.osm.pbf");

  public static final Path DATA_OSC_XML = resolve("data.osc.xml");

  public static final Path DATA_OSM_XML = resolve("data.osm.xml");

  public static final Path SIMPLE_DATA_DIR = resolve("simple");

  public static final Path SIMPLE_DATA_OSM_PBF = resolve("simple/data.osm.pbf");

  public static final Path DENSE_NODES_OSM_PBF = resolve("dense-nodes.osm.pbf");

  public static final Path WAYS_OSM_PBF = resolve("ways.osm.pbf");

  public static final Path RELATIONS_OSM_PBF = resolve("relations.osm.pbf");

  public static final Path LIECHTENSTEIN_DIR = resolve("liechtenstein");

  public static final Path LIECHTENSTEIN_OSM_PBF = resolve("liechtenstein/liechtenstein.osm.pbf");

  public static final Path MONACO_DIR = resolve("monaco");

  public static final Path MONACO_OSC_GZ = resolve("monaco/monaco.osc.gz");

  public static final Path MONACO_OSM_BZ2 = resolve("monaco/monaco.osm.bz2");

  public static final Path MONACO_OSM_PBF = resolve("monaco/monaco.osm.pbf");

  public static final Path MONACO_STATE_TXT = resolve("monaco/monaco-state.txt");

  public static Path resolve(String resource) {
    Path cwd = Path.of("").toAbsolutePath();
    Path pathFromRoot = Path.of("baremaps-core", "src", "test", "resources", resource);
    return cwd.resolveSibling(pathFromRoot);
  }
}
