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

package com.baremaps.postgres.model;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

// https://github.com/npgsql/npgsql/blob/d4132d0d546594629bcef658bcb1418b4a8624cc/src/Npgsql/NpgsqlTypes/NpgsqlRange.cs
public class Range<T> {

  private int flags;

  @Nullable private T lowerBound;

  @Nullable private T upperBound;

  public Range(@Nullable T lowerBound, @Nullable T upperBound) {
    this(lowerBound, true, false, upperBound, true, false);
  }

  public Range(
      @Nullable T lowerBound,
      boolean lowerBoundIsInclusive,
      @Nullable T upperBound,
      boolean upperBoundIsInclusive) {
    this(lowerBound, lowerBoundIsInclusive, false, upperBound, upperBoundIsInclusive, false);
  }

  public Range(
      @Nullable T lowerBound,
      boolean lowerBoundIsInclusive,
      boolean lowerBoundInfinite,
      @Nullable T upperBound,
      boolean upperBoundIsInclusive,
      boolean upperBoundInfinite) {
    this(
        lowerBound,
        upperBound,
        evaluateBoundaryFlags(
            lowerBoundIsInclusive, upperBoundIsInclusive, lowerBoundInfinite, upperBoundInfinite));
  }

  private Range(@Nullable T lowerBound, @Nullable T upperBound, int flags) {
    this.lowerBound = (flags & RangeFlags.LowerBoundInfinite) != 0 ? null : lowerBound;
    this.upperBound = (flags & RangeFlags.UpperBoundInfinite) != 0 ? null : upperBound;
    this.flags = flags;

    // TODO Check this!
    if (lowerBound == null) {
      this.flags |= RangeFlags.LowerBoundInfinite;
    }

    if (upperBound == null) {
      this.flags |= RangeFlags.UpperBoundInfinite;
    }

    if (isEmptyRange(lowerBound, upperBound, flags)) {
      this.lowerBound = null;
      this.upperBound = null;
      this.flags = RangeFlags.Empty;
    }
  }

  private boolean isEmptyRange(@Nullable T lowerBound, @Nullable T upperBound, int flags) {
    // ---------------------------------------------------------------------------------
    // We only want to check for those conditions that are unambiguously erroneous:
    //   1. The bounds must not be default values (including null).
    //   2. The bounds must be definite (non-infinite).
    //   3. The bounds must be inclusive.
    //   4. The bounds must be considered equal.
    //
    // See:
    //  - https://github.com/npgsql/npgsql/pull/1939
    //  - https://github.com/npgsql/npgsql/issues/1943
    // ---------------------------------------------------------------------------------

    if ((flags & RangeFlags.Empty) == RangeFlags.Empty) return true;

    if ((flags & RangeFlags.Infinite) == RangeFlags.Infinite) return false;

    if ((flags & RangeFlags.Inclusive) == RangeFlags.Inclusive) return false;

    return Objects.equals(lowerBound, upperBound);
  }

  private static int evaluateBoundaryFlags(
      boolean lowerBoundIsInclusive,
      boolean upperBoundIsInclusive,
      boolean lowerBoundInfinite,
      boolean upperBoundInfinite) {

    int result = RangeFlags.None;

    // This is the only place flags are calculated.
    if (lowerBoundIsInclusive) result |= RangeFlags.LowerBoundInclusive;
    if (upperBoundIsInclusive) result |= RangeFlags.UpperBoundInclusive;
    if (lowerBoundInfinite) result |= RangeFlags.LowerBoundInfinite;
    if (upperBoundInfinite) result |= RangeFlags.UpperBoundInfinite;

    // PostgreSQL automatically converts inclusive-infinities.
    // See: https://www.postgresql.org/docs/current/static/rangetypes.html#RANGETYPES-INFINITE
    if ((result & RangeFlags.LowerInclusiveInfinite) == RangeFlags.LowerInclusiveInfinite) {
      result &= ~RangeFlags.LowerBoundInclusive;
    }

    if ((result & RangeFlags.UpperInclusiveInfinite) == RangeFlags.UpperInclusiveInfinite) {
      result &= ~RangeFlags.UpperBoundInclusive;
    }

    return result;
  }

  public int getFlags() {
    return flags;
  }

  public boolean isEmpty() {
    return (flags & RangeFlags.Empty) != 0;
  }

  public boolean isLowerBoundInfinite() {
    return (flags & RangeFlags.LowerBoundInfinite) != 0;
  }

  public boolean isUpperBoundInfinite() {
    return (flags & RangeFlags.UpperBoundInfinite) != 0;
  }

  @Nullable
  public T getLowerBound() {
    return lowerBound;
  }

  public void setLowerBound(T lowerBound) {
    this.lowerBound = lowerBound;
  }

  @Nullable
  public T getUpperBound() {
    return upperBound;
  }

  public void setUpperBound(T upperBound) {
    this.upperBound = upperBound;
  }
}
