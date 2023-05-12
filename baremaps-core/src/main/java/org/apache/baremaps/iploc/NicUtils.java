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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility methods for NIC objects.
 */
public class NicUtils {

  private NicUtils() {}

  /**
   * Returns true if the NIC object is an inetnum.
   *
   * @param nicObject
   * @return
   */
  public static boolean isInetnum(NicObject nicObject) {
    return "inetnum".equals(nicObject.type());
  }

  /**
   * Writes a NIC object into a text file.
   *
   * @param path the path of a text file
   */
  public static void writeToFile(Path path, NicObject nicObject) throws IOException {
    if (Files.notExists(path)) {
      Files.createDirectories(path);
    }
    String fileName = nicObject.type() + "-" + nicObject.id() + ".txt";
    FileOutputStream fos = new FileOutputStream(path + "/" + fileName);
    try (DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(fos))) {
      outStream.writeUTF(nicObject.toString());
    }
  }
}
