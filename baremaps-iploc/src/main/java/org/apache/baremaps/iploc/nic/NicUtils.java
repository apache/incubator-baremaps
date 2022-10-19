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

package org.apache.baremaps.iploc.nic;



import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Static methods for processing NIC data. */
public class NicUtils {

  private NicUtils() {}

  /**
   * Writes a NIC object into a text file.
   *
   * @param file the path of a text file
   */
  public static void writeToFile(Path file, NicObject object) throws IOException {
    if (Files.notExists(file)) {
      Files.createDirectories(file);
    }
    String fileName = object.type() + "-" + object.id() + ".txt";
    FileOutputStream fos = new FileOutputStream(file + "/" + fileName);
    try (DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(fos))) {
      outStream.writeUTF(object.toString());
    }
  }
}
