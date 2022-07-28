// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import com.baremaps.postgres.converter.ValueConverter;
import com.baremaps.postgres.converter.LocalDateTimeConverter;

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
    protected void internalHandle(DataOutputStream buffer, final LocalDateTime value) throws IOException {
        buffer.writeInt(8);
        buffer.writeLong(dateTimeConverter.convert(value));
    }
}
