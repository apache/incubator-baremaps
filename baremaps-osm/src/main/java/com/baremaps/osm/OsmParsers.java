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

package com.baremaps.osm;

import com.baremaps.osm.change.OsmChangeParser;
import com.baremaps.osm.pbf.OsmPbfParser;
import com.baremaps.osm.state.OsmStateParser;
import com.baremaps.osm.xml.OsmXmlParser;

/** Utility methods for creating OpenStreetMap parsers. */
public class OsmParsers {

  public static OsmStateParser state() {
    return new OsmStateParser();
  }

  public static OsmPbfParser pbf() {
    return new OsmPbfParser();
  }

  public static OsmXmlParser xml() {
    return new OsmXmlParser();
  }

  public static OsmChangeParser change() {
    return new OsmChangeParser();
  }
}
