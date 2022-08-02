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

package com.baremaps.postgres.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class TimeStampUtils {

  private TimeStampUtils() {}

  private static final LocalDateTime JavaEpoch = LocalDateTime.of(1970, 1, 1, 0, 0, 0);

  private static final LocalDateTime PostgresEpoch = LocalDateTime.of(2000, 1, 1, 0, 0, 0);

  private static final long DaysBetweenJavaAndPostgresEpochs =
      ChronoUnit.DAYS.between(JavaEpoch, PostgresEpoch);

  public static long convertToPostgresTimeStamp(LocalDateTime localDateTime) {

    if (localDateTime == null) {
      throw new IllegalArgumentException("localDateTime");
    }
    // Extract the Time of the Day in Nanoseconds:
    long timeInNanoseconds = localDateTime.toLocalTime().toNanoOfDay();

    // Convert the Nanoseconds to Microseconds:
    long timeInMicroseconds = timeInNanoseconds / 1000;

    // Now Calculate the Postgres Timestamp:
    if (localDateTime.isBefore(PostgresEpoch)) {
      long dateInMicroseconds =
          (localDateTime.toLocalDate().toEpochDay() - DaysBetweenJavaAndPostgresEpochs)
              * 86400000000L;

      return dateInMicroseconds + timeInMicroseconds;
    } else {
      long dateInMicroseconds =
          (DaysBetweenJavaAndPostgresEpochs - localDateTime.toLocalDate().toEpochDay())
              * 86400000000L;

      return -(dateInMicroseconds - timeInMicroseconds);
    }
  }

  public static int toPgDays(LocalDate date) {
    // Adjust TimeZone Offset:
    LocalDateTime dateTime = date.atStartOfDay();
    // pg time 0 is 2000-01-01 00:00:00:
    long secs = toPgSecs(getSecondsSinceJavaEpoch(dateTime));
    // Needs Days:
    return (int) TimeUnit.SECONDS.toDays(secs);
  }

  public static Long toPgSecs(LocalDateTime dateTime) {
    // pg time 0 is 2000-01-01 00:00:00:
    long secs = toPgSecs(getSecondsSinceJavaEpoch(dateTime));
    // Needs Microseconds:
    return TimeUnit.SECONDS.toMicros(secs);
  }

  private static long getSecondsSinceJavaEpoch(LocalDateTime localDateTime) {
    // Adjust TimeZone Offset:
    OffsetDateTime zdt = localDateTime.atOffset(ZoneOffset.UTC);
    // Get the Epoch Milliseconds:
    long milliseconds = zdt.toInstant().toEpochMilli();
    // Turn into Seconds:
    return TimeUnit.MILLISECONDS.toSeconds(milliseconds);
  }

  /**
   * Converts the given java seconds to postgresql seconds. The conversion is valid for any year 100
   * BC onwards.
   *
   * <p>from /org/postgresql/jdbc2/TimestampUtils.java
   *
   * @param seconds Postgresql seconds.
   * @return Java seconds.
   */
  @SuppressWarnings("checkstyle:magicnumber")
  private static long toPgSecs(final long seconds) {
    long secs = seconds;
    // java epoc to postgres epoc
    secs -= 946684800L;

    // Julian/Greagorian calendar cutoff point
    if (secs < -13165977600L) { // October 15, 1582 -> October 4, 1582
      secs -= 86400 * 10;
      if (secs < -15773356800L) { // 1500-03-01 -> 1500-02-28
        int years = (int) ((secs + 15773356800L) / -3155823050L);
        years++;
        years -= years / 4;
        secs += years * 86400;
      }
    }

    return secs;
  }
}
