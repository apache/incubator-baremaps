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

package org.apache.baremaps.utils;

public class ArrayUtils {

  private ArrayUtils() {

  }

  public static Object of(int dimensions, int size) {
    return switch (dimensions) {
      case 1 -> d1(size);
      case 2 -> d2(size);
      case 3 -> d3(size);
      case 4 -> d4(size);
      case 5 -> d5(size);
      case 6 -> d6(size);
      case 7 -> d7(size);
      case 8 -> d8(size);
      case 9 -> d9(size);
      case 10 -> d10(size);
      default -> throw new IllegalArgumentException("Unsupported dimension");
    };
  }

  private static Object d1(int size) {
    return new long[size];
  }

  private static Object d2(int size) {
    return new long[size][];
  }

  private static Object d3(int size) {
    return new long[size][][];
  }

  private static Object d4(int size) {
    return new long[size][][][];
  }

  private static Object d5(int size) {
    return new long[size][][][][];
  }

  private static Object d6(int size) {
    return new long[size][][][][][];
  }

  private static Object d7(int size) {
    return new long[size][][][][][][];
  }

  private static Object d8(int size) {
    return new long[size][][][][][][][];
  }

  private static Object d9(int size) {
    return new long[size][][][][][][][][];
  }

  public static Object d10(int size) {
    return new long[size][][][][][][][][][];
  }
}
