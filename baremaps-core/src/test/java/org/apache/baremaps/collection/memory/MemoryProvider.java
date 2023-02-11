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



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

public class MemoryProvider {

  public static final int SEGMENT_BYTES = 1 << 10;

  public static Stream<Arguments> memories() throws IOException {
    return Stream.of(Arguments.of(new OnHeapMemory(SEGMENT_BYTES)),
        Arguments.of(new OffHeapMemory(SEGMENT_BYTES)),
        Arguments.of(new MemoryMappedFile(
            Files.createTempFile(Paths.get("."), "baremaps_", ".tmp"), SEGMENT_BYTES)),
        Arguments.of(new MemoryMappedFile(
            Files.createTempFile(Paths.get("."), "baremaps_", ".tmp"), SEGMENT_BYTES)));
  }
}
