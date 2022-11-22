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
import java.util.Objects;
import java.util.StringJoiner;

/** Represents a header entity in an OpenStreetMap dataset. */
public final class Header implements Entity {

  private final Long replicationSequenceNumber;
  private final LocalDateTime replicationTimestamp;
  private final String replicationUrl;
  private final String source;
  private final String writingProgram;

  /**
   * Constructs an OpenStreetMap {@code Header} based on the specified parameters.
   *
   * @param replicationSequenceNumber the replication sequence number
   * @param replicationTimestamp the replication timestamp
   * @param replicationUrl the replication url
   * @param source the source
   * @param writingProgram the writing program
   */
  public Header(Long replicationSequenceNumber, LocalDateTime replicationTimestamp,
      String replicationUrl, String source, String writingProgram) {
    this.replicationTimestamp = replicationTimestamp;
    this.replicationSequenceNumber = replicationSequenceNumber;
    this.replicationUrl = replicationUrl;
    this.source = source;
    this.writingProgram = writingProgram;
  }

  /**
   * Returns the replication timestamp.
   *
   * @return the replication timestamp
   */
  public LocalDateTime getReplicationTimestamp() {
    return replicationTimestamp;
  }

  /**
   * Returns the replication sequence number.
   *
   * @return the replication sequence number
   */
  public Long getReplicationSequenceNumber() {
    return replicationSequenceNumber;
  }

  /**
   * Returns the replication url.
   *
   * @return the replication url
   */
  public String getReplicationUrl() {
    return replicationUrl;
  }

  /**
   * Returns the source.
   *
   * @return the source
   */
  public String getSource() {
    return source;
  }

  /**
   * Returns the writing program.
   *
   * @return the writing program
   */
  public String getWritingProgram() {
    return writingProgram;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Header)) {
      return false;
    }
    Header header = (Header) o;
    return Objects.equals(replicationTimestamp, header.replicationTimestamp)
        && Objects.equals(replicationSequenceNumber, header.replicationSequenceNumber)
        && Objects.equals(replicationUrl, header.replicationUrl)
        && Objects.equals(source, header.source)
        && Objects.equals(writingProgram, header.writingProgram);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hash(replicationTimestamp, replicationSequenceNumber, replicationUrl, source,
        writingProgram);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return new StringJoiner(", ", Header.class.getSimpleName() + "[", "]")
        .add("replicationTimestamp=" + replicationTimestamp)
        .add("replicationSequenceNumber=" + replicationSequenceNumber)
        .add("replicationUrl='" + replicationUrl + "'").add("source='" + source + "'")
        .add("writingProgram='" + writingProgram + "'").toString();
  }
}
