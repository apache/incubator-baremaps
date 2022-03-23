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

package com.baremaps.blob;

/** Signals that an exception occurred in a {@code BlobStore}. */
public class BlobStoreException extends Exception {
  /** Constructs a {@code BlobStoreException} with {@code null} as its error detail message. */
  public BlobStoreException() {}

  /**
   * Constructs an {@code BlobStoreException} with the specified detail message.
   *
   * @param message the message
   */
  public BlobStoreException(String message) {
    super(message);
  }

  /**
   * Constructs a {@code BlobStoreException} with the specified cause.
   *
   * @param cause the cause
   */
  public BlobStoreException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a {@code BlobStoreException} with the specified detail message and cause.
   *
   * @param message the message
   * @param cause the cause
   */
  public BlobStoreException(String message, Throwable cause) {
    super(message, cause);
  }
}
