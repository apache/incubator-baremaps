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
