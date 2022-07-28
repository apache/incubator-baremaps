// Licensed under the MIT license. See LICENSE file in the project root for full license information.

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
