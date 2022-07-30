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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class ZonedDateTimeValueHandler extends BaseValueHandler<ZonedDateTime> {

  private ValueConverter<ZonedDateTime, Long> dateTimeConverter;

  public ZonedDateTimeValueHandler() {
    this(new ToUTCStripTimezone());
  }

  public ZonedDateTimeValueHandler(ValueConverter<ZonedDateTime, Long> dateTimeConverter) {
    this.dateTimeConverter = dateTimeConverter;
  }

  @Override
  protected void internalHandle(DataOutputStream buffer, ZonedDateTime value) throws IOException {
    buffer.writeInt(8);
    buffer.writeLong(dateTimeConverter.convert(value));
  }

  private static final class ToUTCStripTimezone implements ValueConverter<ZonedDateTime, Long> {
    private final ValueConverter<LocalDateTime, Long> converter = new LocalDateTimeConverter();

    @Override
    public Long convert(final ZonedDateTime value) {
      return converter.convert(value.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
    }
  }
}
