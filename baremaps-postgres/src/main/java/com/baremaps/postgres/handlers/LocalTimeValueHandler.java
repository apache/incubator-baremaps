// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import com.baremaps.postgres.converter.ValueConverter;
import com.baremaps.postgres.converter.LocalTimeConverter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalTime;

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
