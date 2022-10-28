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

package org.apache.baremaps.openstreetmap;



import org.apache.baremaps.openstreetmap.pbf.PbfBlockReader;
import org.apache.baremaps.openstreetmap.state.StateReader;
import org.apache.baremaps.openstreetmap.xml.XmlChangeReader;
import org.apache.baremaps.openstreetmap.xml.XmlEntityReader;

/** Utility methods for creating OpenStreetMap parsers. */
public class OsmReaders {

  public static StateReader state() {
    return new StateReader();
  }

  public static PbfBlockReader pbf() {
    return new PbfBlockReader();
  }

  public static XmlEntityReader xml() {
    return new XmlEntityReader();
  }

  public static XmlChangeReader change() {
    return new XmlChangeReader();
  }
}
