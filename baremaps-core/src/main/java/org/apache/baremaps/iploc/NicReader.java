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

package org.apache.baremaps.iploc;



import com.google.common.base.Charsets;
import java.io.*;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** A parser for Network Coordination Center (NIC) data. */
public class NicReader {

  public NicReader() {}

  /**
   * Creates an ordered stream of NIC objects.
   *
   * @param inputStream a {@link InputStream} containing NIC objects
   * @return a {@link Stream} of NIC Object
   */
  public Stream<NicObject> read(InputStream inputStream) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charsets.UTF_8));
    Spliterator<String> spliterator = reader.lines().spliterator();
    return StreamSupport.stream(new NicSpliterator(spliterator), false);
  }
}
