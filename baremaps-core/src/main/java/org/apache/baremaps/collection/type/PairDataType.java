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

package org.apache.baremaps.collection.type;



import java.nio.ByteBuffer;
import java.util.Objects;
import org.apache.baremaps.collection.type.PairDataType.Pair;

/** A {@link DataType} for reading and writing pairs of values in {@link ByteBuffer}s. */
public class PairDataType<L, R> extends FixedSizeDataType<Pair<L, R>> {

  private final FixedSizeDataType<L> left;
  private final FixedSizeDataType<R> right;

  public PairDataType(FixedSizeDataType<L> left, FixedSizeDataType<R> right) {
    super(left.size() + right.size());
    this.left = left;
    this.right = right;
  }

  /** {@inheritDoc} */
  @Override
  public void write(ByteBuffer buffer, int position, Pair<L, R> value) {
    left.write(buffer, position, value.left());
    right.write(buffer, position + left.size(), value.right());
  }

  /** {@inheritDoc} */
  @Override
  public Pair<L, R> read(ByteBuffer buffer, int position) {
    return new Pair<>(
        left.read(buffer, position),
        right.read(buffer, position + left.size()));
  }

  public static class Pair<L, R> {

    private final L left;

    private final R right;

    public Pair(L left, R right) {
      this.left = left;
      this.right = right;
    }

    public L left() {
      return left;
    }

    public R right() {
      return right;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof PairDataType.Pair)) {
        return false;
      }

      Pair<?, ?> pair = (Pair<?, ?>) o;

      if (!Objects.equals(left, pair.left)) {
        return false;
      }

      return Objects.equals(right, pair.right);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
      int result = left != null ? left.hashCode() : 0;
      result = 31 * result + (right != null ? right.hashCode() : 0);
      return result;
    }
  }
}
