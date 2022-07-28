// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.converter;

import com.baremaps.postgres.util.TimeStampUtils;

import java.time.LocalDateTime;

public class LocalDateTimeConverter implements ValueConverter<LocalDateTime, Long> {
    @Override
    public Long convert(final LocalDateTime dateTime) {
        return TimeStampUtils.convertToPostgresTimeStamp(dateTime);
    }
}
