/*
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

package org.apache.baremaps.postgres.handlers;



import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.apache.baremaps.postgres.util.BigDecimalUtils;

/**
 * The Algorithm for turning a BigDecimal into a Postgres Numeric is heavily inspired by the
 * Intermine Implementation:
 *
 * <p>
 * https://github.com/intermine/intermine/blob/master/intermine/objectstore/main/src/org/intermine/sql/writebatch/BatchWriterPostgresCopyImpl.java
 */
public class BigDecimalValueHandler<T extends Number> extends BaseValueHandler<T> {

  private static final int DECIMAL_DIGITS = 4;
  private static final BigInteger TEN_THOUSAND = new BigInteger("10000");

  @Override
  protected void internalHandle(final DataOutputStream buffer, final T value) throws IOException {
    final BigDecimal tmpValue = getNumericAsBigDecimal(value);

    // Number of fractional digits:
    final int fractionDigits = tmpValue.scale();

    // Number of Fraction Groups:
    final int fractionGroups = (fractionDigits + 3) / 4;

    final List<Integer> digits = digits(tmpValue);

    buffer.writeInt(8 + (2 * digits.size()));
    buffer.writeShort(digits.size());
    buffer.writeShort(digits.size() - fractionGroups - 1);
    buffer.writeShort(tmpValue.signum() == 1 ? 0x0000 : 0x4000);
    buffer.writeShort(fractionDigits);

    // Now write each digit:
    for (int pos = digits.size() - 1; pos >= 0; pos--) {
      final int valueToWrite = digits.get(pos);
      buffer.writeShort(valueToWrite);
    }
  }

  private static BigDecimal getNumericAsBigDecimal(final Number source) {
    if (!(source instanceof BigDecimal)) {
      return BigDecimalUtils.toBigDecimal(source.doubleValue());
    }

    return (BigDecimal) source;
  }

  private List<Integer> digits(final BigDecimal value) {
    BigInteger unscaledValue = value.unscaledValue();

    if (value.signum() == -1) {
      unscaledValue = unscaledValue.negate();
    }

    final List<Integer> digits = new ArrayList<>();

    // The scale needs to be a multiple of 4:
    int scaleRemainder = value.scale() % 4;

    // Scale the first value:
    if (scaleRemainder != 0) {
      final BigInteger[] result =
          unscaledValue.divideAndRemainder(BigInteger.TEN.pow(scaleRemainder));
      final int digit =
          result[1].intValue() * (int) Math.pow(10, (double) DECIMAL_DIGITS - scaleRemainder);
      digits.add(digit);
      unscaledValue = result[0];
    }

    while (!unscaledValue.equals(BigInteger.ZERO)) {
      final BigInteger[] result = unscaledValue.divideAndRemainder(TEN_THOUSAND);
      digits.add(result[1].intValue());
      unscaledValue = result[0];
    }

    return digits;
  }
}
