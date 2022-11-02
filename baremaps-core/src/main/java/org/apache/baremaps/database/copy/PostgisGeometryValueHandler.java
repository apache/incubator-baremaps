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

package org.apache.baremaps.database.copy;

import static org.locationtech.jts.io.WKBConstants.wkbNDR;

import de.bytefish.pgbulkinsert.pgsql.handlers.BaseValueHandler;
import java.io.DataOutputStream;
import java.io.IOException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;

public class PostgisGeometryValueHandler extends BaseValueHandler<Geometry> {

  @Override
  protected void internalHandle(DataOutputStream buffer, Geometry value) throws IOException {
    WKBWriter writer = new WKBWriter(2, wkbNDR, true);
    byte[] wkb = writer.write(value);
    buffer.writeInt(wkb.length);
    buffer.write(wkb, 0, wkb.length);
  }

  @Override
  public int getLength(Geometry geometry) {
    throw new UnsupportedOperationException();
  }
}
