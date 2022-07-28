// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import com.baremaps.postgres.converter.ValueConverter;
import com.baremaps.postgres.converter.LocalDateConverter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;

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
