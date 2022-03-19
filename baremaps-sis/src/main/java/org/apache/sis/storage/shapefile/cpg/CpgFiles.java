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

package org.apache.sis.storage.shapefile.cpg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.sis.util.Static;

/**
 * CPG files utilities. {@code *.cpg} files contains a single word for the name of the DBF character
 * encoding.
 *
 * @author Johann Sorel (Geomatys)
 * @version 2.0
 * @since 2.0
 * @module
 */
public final class CpgFiles extends Static {
  /** Do not allow instantiation of this class. */
  private CpgFiles() {}

  /**
   * Reads the character set from given stream. If the file is empty, then the default character set
   * is returned.
   *
   * @param in input channel from which to read the character set.
   * @return the character set from the given stream.
   * @throws IOException if the file does not exist or cannot be read.
   */
  public static Charset read(final ReadableByteChannel in) throws IOException {
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(Channels.newInputStream(in), StandardCharsets.US_ASCII))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (!(line = line.trim()).isEmpty()) {
          return Charset.forName(line);
        }
      }
    }
    return Charset.defaultCharset();
  }

  /**
   * Writes the character set to given file.
   *
   * @param cs character set to write.
   * @param file output file.
   * @throws IOException if an error occurred while writing the file.
   */
  public static void write(final Charset cs, final Path file) throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.US_ASCII)) {
      writer.write(cs.name());
    }
  }
}
