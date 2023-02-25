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

package org.apache.baremaps.dataframe;

/** Signals that an exception occurred in a data frame. */
public class DataFrameException extends RuntimeException {
  /** Constructs a {@code DataFrameException} with {@code null} as its error detail message. */
  public DataFrameException() {}

  /**
   * Constructs an {@code DataFrameException} with the specified detail message.
   *
   * @param message the message
   */
  public DataFrameException(String message) {
    super(message);
  }

  /**
   * Constructs a {@code DataFrameException} with the specified cause.
   *
   * @param cause the cause
   */
  public DataFrameException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a {@code DataFrameException} with the specified detail message and cause.
   *
   * @param message the message
   * @param cause the cause
   */
  public DataFrameException(String message, Throwable cause) {
    super(message, cause);
  }
}
