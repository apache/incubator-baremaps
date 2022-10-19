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



import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import org.apache.baremaps.postgres.converter.LocalTimeConverter;
import org.apache.baremaps.postgres.converter.ValueConverter;

public class LocalTimeValueHandler extends BaseValueHandler<LocalTime> {

  private ValueConverter<LocalTime, Long> timeConverter;

  public LocalTimeValueHandler() {
    this(new LocalTimeConverter());
  }

  public LocalTimeValueHandler(ValueConverter<LocalTime, Long> timeConverter) {
    this.timeConverter = timeConverter;
  }

  @Override
  protected void internalHandle(DataOutputStream buffer, final LocalTime value) throws IOException {
    buffer.writeInt(8);
    buffer.writeLong(timeConverter.convert(value));
  }
}
