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

package com.baremaps.postgres.handlers;

import com.baremaps.postgres.converter.LocalDateTimeConverter;
import com.baremaps.postgres.converter.ValueConverter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

public class LocalDateTimeValueHandler extends BaseValueHandler<LocalDateTime> {

  private ValueConverter<LocalDateTime, Long> dateTimeConverter;

  public LocalDateTimeValueHandler() {
    this(new LocalDateTimeConverter());
  }

  public LocalDateTimeValueHandler(ValueConverter<LocalDateTime, Long> dateTimeConverter) {

    this.dateTimeConverter = dateTimeConverter;
  }

  @Override
  protected void internalHandle(DataOutputStream buffer, final LocalDateTime value)
      throws IOException {
    buffer.writeInt(8);
    buffer.writeLong(dateTimeConverter.convert(value));
  }
}
