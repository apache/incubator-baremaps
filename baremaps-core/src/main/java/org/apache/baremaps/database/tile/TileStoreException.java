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

package org.apache.baremaps.database.tile;

/** Signals that an exception occurred in a {@code TileStore}. */
public class TileStoreException extends Exception {

  /**
   * Constructs an {@code BlobStoreException} with the specified detail message.
   *
   * @param message the message
   */
  public TileStoreException(String message) {
    super(message);
  }

  /**
   * Constructs a {@code BlobStoreException} with the specified cause.
   *
   * @param cause the cause
   */
  public TileStoreException(Throwable cause) {
    super(cause);
  }
}
