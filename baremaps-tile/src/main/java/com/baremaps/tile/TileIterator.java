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
package com.baremaps.tile;

import static com.baremaps.tile.Tile.max;
import static com.baremaps.tile.Tile.min;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.locationtech.jts.geom.Envelope;

class TileIterator implements Iterator<Tile> {

  private final Envelope envelope;

  private final int zoomMax;

  private int z;

  private int x;

  private int y;

  public TileIterator(Envelope envelope, int zoomMin, int zoomMax) {
    this.envelope = envelope;
    this.zoomMax = zoomMax;
    this.z = zoomMin;
    Tile min = min(envelope, this.z);
    this.x = min.x();
    this.y = min.y();
  }

  @Override
  public boolean hasNext() {
    Tile max = max(envelope, this.z);
    return x <= max.x() && y <= max.y() && z <= zoomMax;
  }

  @Override
  public Tile next() {
    Tile tile = new Tile(x, y, z);
    Tile max = max(envelope, this.z);
    if (z > max.z()) {
      throw new NoSuchElementException();
    }
    if (x < max.x()) {
      x++;
    } else if (y < max.y()) {
      y++;
      Tile min = min(envelope, this.z);
      x = min.x();
    } else {
      z++;
      Tile min = min(envelope, this.z);
      x = min.x();
      y = min.y();
    }
    return tile;
  }
}
