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

package org.apache.baremaps.openstreetmap.function;



import java.util.function.Function;
import org.apache.baremaps.openstreetmap.model.Blob;
import org.apache.baremaps.openstreetmap.model.Block;
import org.apache.baremaps.openstreetmap.pbf.DataBlockReader;
import org.apache.baremaps.openstreetmap.pbf.HeaderBlockReader;
import org.apache.baremaps.stream.StreamException;

/**
 * Maps a blob to a block.
 */
public class BlobToBlockMapper implements Function<Blob, Block> {

  /** {@inheritDoc} */
  @Override
  public Block apply(Blob blob) {
    try {
      switch (blob.header().getType()) {
        case "OSMHeader":
          return new HeaderBlockReader(blob).read();
        case "OSMData":
          return new DataBlockReader(blob).read();
        default:
          throw new StreamException("Unknown blob type");
      }
    } catch (StreamException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new StreamException(exception);
    }
  }
}
