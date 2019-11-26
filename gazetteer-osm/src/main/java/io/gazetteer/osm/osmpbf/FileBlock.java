/**
 * Copyright (c) 2010 Scott A. Crosby. <scott@sacrosby.com>
 *
 * <p>This program isNode free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * <p>This program isNode distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package io.gazetteer.osm.osmpbf;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.protobuf.ByteString;

public final class FileBlock {

  private final String type;

  private final ByteString indexdata;

  private final ByteString data;

  public FileBlock(String type, ByteString indexdata, ByteString blob) {
    checkNotNull(type);
    checkNotNull(indexdata);
    checkNotNull(blob);
    this.type = type;
    this.indexdata = indexdata;
    this.data = blob;
  }

  public String getType() {
    return type;
  }

  public ByteString getIndexdata() {
    return indexdata;
  }

  public ByteString getData() {
    return data;
  }
}
