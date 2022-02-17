/*
 * Copyright (C) 2021 Heig-vd
 *
 * Licensed under the “Commons Clause” License Condition v1.0. You may obtain a copy of the License at
 *
 * https://github.com/heigvd-software-engineering/netscan/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.baremaps.nic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/** Nic spliterator. */
public class NiceSpliterator implements Spliterator<NiceObject> {

  private final BufferedReader reader;

  /** {@inheritdoc} */
  public NiceSpliterator(InputStream inputStream) {
    this.reader = new BufferedReader(new InputStreamReader(inputStream));
  }

  public long estimateSize() {
    return Long.MAX_VALUE;
  }

  public int characteristics() {
    return IMMUTABLE;
  }

  public Spliterator<NiceObject> trySplit() {
    return null;
  }

  public boolean tryAdvance(Consumer<? super NiceObject> consumer) {
    try {
      String line;
      StringBuilder key = new StringBuilder();
      StringBuilder val = new StringBuilder();
      List<NicAttribute> attributes = new ArrayList<>();

      while ((line = reader.readLine()) != null && !"".equals(line)) {

        // handle multiline values
        if (line.startsWith(" ")) {
          val.append("\n");
          val.append(line.trim());
        }

        // handle multiline values
        else if (line.startsWith("+")) {
          val.append("\n");
          val.append(line.substring(1).trim());
        }

        // handle attribute line
        else if (!line.startsWith("#") && !line.startsWith("%")) {
          int index = line.indexOf(":");
          if (index >= 0) {
            addAttributes(key, val, attributes);
            key = new StringBuilder();
            val = new StringBuilder();
            key.append(line.substring(0, index).trim());
            val.append(line.substring(index + 1).trim());
          }
        }
      }

      // handle last attribute
      addAttributes(key, val, attributes);

      // build object
      if (!attributes.isEmpty()) {
        consumer.accept(new NiceObject(attributes));
      }

      return line != null;
    } catch (IOException e) {
      return false;
    }
  }

  private void addAttributes(StringBuilder key, StringBuilder val, List<NicAttribute> attributes) {
    if (key.length() > 0) {
      attributes.add(new NicAttribute(key.toString(), val.toString()));
    }
  }
}
