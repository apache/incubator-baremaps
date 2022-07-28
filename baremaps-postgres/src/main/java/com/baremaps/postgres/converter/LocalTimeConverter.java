// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.converter;

import java.time.LocalTime;

public class LocalTimeConverter implements ValueConverter<LocalTime, Long> {

    @Override
    public Long convert(final LocalTime time) {
        return time.toNanoOfDay() / 1000L;
    }

}
