// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.converter;


import com.baremaps.postgres.util.TimeStampUtils;
import java.time.LocalDate;

public class LocalDateConverter implements ValueConverter<LocalDate, Integer> {

    @Override
    public Integer convert(final LocalDate date) {
        return TimeStampUtils.toPgDays(date);
    }

}
