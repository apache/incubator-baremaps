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

import java.math.BigDecimal;
import java.math.MathContext;

public final class BigDecimalUtils {

  private BigDecimalUtils() {}

  public static BigDecimal toBigDecimal(Integer intValue) {
    return new BigDecimal(intValue.toString());
  }

  public static BigDecimal toBigDecimal(Integer intValue, MathContext mathContext) {
    return new BigDecimal(intValue.toString(), mathContext);
  }

  public static BigDecimal toBigDecimal(Long longValue) {
    return new BigDecimal(longValue.toString());
  }

  public static BigDecimal toBigDecimal(Long longValue, MathContext mathContext) {
    return new BigDecimal(longValue.toString(), mathContext);
  }

  public static BigDecimal toBigDecimal(Float floatValue) {
    return new BigDecimal(floatValue.toString());
  }

  public static BigDecimal toBigDecimal(Float floatValue, MathContext mathContext) {
    return new BigDecimal(floatValue.toString(), mathContext);
  }

  public static BigDecimal toBigDecimal(Double doubleValue) {
    return new BigDecimal(doubleValue.toString());
  }

  public static BigDecimal toBigDecimal(Double doubleValue, MathContext mathContext) {
    return new BigDecimal(doubleValue.toString(), mathContext);
  }
}
