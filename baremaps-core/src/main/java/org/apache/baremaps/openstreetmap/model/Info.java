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

/** Represents all the metadata associated to an element in an OpenStreetMap dataset. */
public record Info(int version, LocalDateTime timestamp, long changeset, int uid) {

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Info)) {
      return false;
    }
    Info info = (Info) o;
    return version == info.version && changeset == info.changeset && uid == info.uid
        && Objects.equals(timestamp, info.timestamp);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hash(version, timestamp, changeset, uid);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return new StringJoiner(", ", Info.class.getSimpleName() + "[", "]").add("version=" + version)
        .add("timestamp=" + timestamp).add("changeset=" + changeset).add("uid=" + uid).toString();
  }
}
