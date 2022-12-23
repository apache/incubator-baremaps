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

package org.apache.baremaps.openstreetmap.xml;



import java.io.InputStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.baremaps.openstreetmap.OsmReader;
import org.apache.baremaps.openstreetmap.model.Entity;

/** A utility class for parsing an OpenStreetMap XML file. */
public class XmlEntityReader implements OsmReader<Entity> {

  /**
   * Creates an ordered stream of OSM entities from a XML file.
   *
   * @param input
   * @return
   */
  public Stream<Entity> stream(InputStream input) {
    return StreamSupport.stream(new XmlEntitySpliterator(input), false);
  }
}
