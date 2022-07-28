package com.baremaps.postgres.handlers;

import com.baremaps.postgres.converter.ValueConverter;
import com.baremaps.postgres.converter.LocalDateTimeConverter;
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