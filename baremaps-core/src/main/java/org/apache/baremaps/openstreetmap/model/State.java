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

package org.apache.baremaps.openstreetmap.model;



import java.time.LocalDateTime;

/** Represents the state of an OpenStreetMap dataset, enabling its replication. */
public class State {

  private final long sequenceNumber;

  private final LocalDateTime timestamp;

  /**
   * Constructs an OpenStreetMap {@code State} with the specified parameters.
   *
   * @param sequenceNumber the sequence number
   * @param timestamp the timestamp
   */
  public State(long sequenceNumber, LocalDateTime timestamp) {
    this.sequenceNumber = sequenceNumber;
    this.timestamp = timestamp;
  }

  /**
   * Returns the sequence number.
   *
   * @return the sequence number
   */
  public long getSequenceNumber() {
    return sequenceNumber;
  }

  /**
   * Returns the timestamp.
   *
   * @return the timestamp
   */
  public LocalDateTime getTimestamp() {
    return timestamp;
  }
}
