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
import java.time.LocalDate;
import org.apache.baremaps.postgres.converter.LocalDateConverter;
import org.apache.baremaps.postgres.converter.ValueConverter;

public class LocalDateValueHandler extends BaseValueHandler<LocalDate> {

  private ValueConverter<LocalDate, Integer> dateConverter;

  public LocalDateValueHandler() {
    this(new LocalDateConverter());
  }

  public LocalDateValueHandler(ValueConverter<LocalDate, Integer> dateTimeConverter) {
    this.dateConverter = dateTimeConverter;
  }

  @Override
  protected void internalHandle(DataOutputStream buffer, final LocalDate value) throws IOException {
    buffer.writeInt(4);
    buffer.writeInt(dateConverter.convert(value));
  }
}
