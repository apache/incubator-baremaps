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

package org.apache.baremaps.database.tile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

class TileIteratorTest {

  @Test
  void iterator() {
    Envelope e = new Tile(0, 0, 0).envelope();

    TileIterator i0 = new TileIterator(e, 0, 0);
    List<Tile> l0 = ImmutableList.copyOf(i0);
    assertEquals(l0.size(), 1);

    TileIterator i1 = new TileIterator(e, 1, 1);
    List<Tile> l1 = ImmutableList.copyOf(i1);
    assertEquals(l1.size(), 4);

    TileIterator i2 = new TileIterator(e, 2, 2);
    List<Tile> l2 = ImmutableList.copyOf(i2);
    assertEquals(l2.size(), 16);
  }

  @Test
  void partial() {
    Envelope e0 = new Tile(0, 0, 1).envelope();
    TileIterator i0 = new TileIterator(e0, 2, 2);
    List<Tile> l0 = ImmutableList.copyOf(i0);
    assertEquals(l0.size(), 4);

    Envelope e1 = new Tile(1, 1, 1).envelope();
    TileIterator i1 = new TileIterator(e1, 2, 2);
    List<Tile> l1 = ImmutableList.copyOf(i1);
    assertEquals(l1.size(), 4);
  }
}
