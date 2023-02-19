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

package org.apache.baremaps.collection.memory;

/** Signals that an exception occurred in a memory. */
public class MemoryException extends RuntimeException {
  /** Constructs a {@link MemoryException} with {@code null} as its error detail message. */
  public MemoryException() {}

  /**
   * Constructs an {@link MemoryException} with the specified detail message.
   *
   * @param message the message
   */
  public MemoryException(String message) {
    super(message);
  }

  /**
   * Constructs a {@link MemoryException} with the specified cause.
   *
   * @param cause the cause
   */
  public MemoryException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a {@link MemoryException} with the specified detail message and cause.
   *
   * @param message the message
   * @param cause the cause
   */
  public MemoryException(String message, Throwable cause) {
    super(message, cause);
  }
}
