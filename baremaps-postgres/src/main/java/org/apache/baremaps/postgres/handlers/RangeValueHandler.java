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

package org.apache.baremaps.postgres.handlers;



import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.baremaps.postgres.model.Range;

public class RangeValueHandler<T> extends BaseValueHandler<Range<T>> {

  private final ValueHandler<T> valueHandler;

  public RangeValueHandler(ValueHandler<T> valueHandler) {
    this.valueHandler = valueHandler;
  }

  @Override
  protected void internalHandle(DataOutputStream buffer, Range<T> value) throws IOException {
    var length = 1;
    var bytes = new ByteArrayOutputStream();
    var output = new DataOutputStream(bytes);

    if (!value.isLowerBoundInfinite()) {
      length += 4;
      valueHandler.handle(output, value.getLowerBound());
    }

    if (!value.isUpperBoundInfinite()) {
      length += 4;
      valueHandler.handle(output, value.getUpperBound());
    }

    length += bytes.size();

    buffer.writeInt(length);
    buffer.writeByte(value.getFlags());

    if (value.isEmpty()) {
      return;
    }

    buffer.write(bytes.toByteArray());
  }
}
