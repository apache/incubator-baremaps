// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.model;

// https://github.com/npgsql/npgsql/blob/d4132d0d546594629bcef658bcb1418b4a8624cc/src/Npgsql/NpgsqlTypes/NpgsqlRange.cs
public class RangeFlags {

    public static final int None = 0;

    public static final int Empty = 1;

    public static final int LowerBoundInclusive = 2;

    public static final int UpperBoundInclusive = 4;

    public static final int LowerBoundInfinite = 8;

    public static final int UpperBoundInfinite = 16;

    public static final int Inclusive = LowerBoundInclusive | UpperBoundInclusive;

    public static final int Infinite = LowerBoundInfinite | UpperBoundInfinite;

    public static final int LowerInclusiveInfinite = LowerBoundInclusive | LowerBoundInfinite;

    public static final int UpperInclusiveInfinite = UpperBoundInclusive | UpperBoundInfinite;
}
