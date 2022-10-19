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

package org.apache.baremaps.storage.shapefile.internal;



import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a ShapefileType Enumeration
 *
 * <p>
 * <div class="warning">This is an experimental class, not yet target for any Apache SIS release at
 * this time.</div>
 *
 * @author Travis L. Pinney
 * @see <a href="http://www.esri.com/library/whitepapers/pdfs/shapefile.pdf">ESRI Shapefile
 *      Specification</a>
 */
public enum ShapeType {
  NullShape(0), Point(1), PolyLine(3), Polygon(5), MultiPoint(8), PointZ(11), PolyLineZ(
      13), PolygonZ(15), MultiPointZ(
          18), PointM(21), PolyLineM(23), PolygonM(25), MultiPointM(28), MultiPatch(31);

  // used for initializing the enumeration
  private int value;

  private ShapeType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  private static final Map<Integer, ShapeType> lookup = new HashMap<Integer, ShapeType>();

  static {
    for (ShapeType ste : EnumSet.allOf(ShapeType.class)) {
      lookup.put(ste.getValue(), ste);
    }
  }

  public static ShapeType get(int value) {
    return lookup.get(value);
  }
}
