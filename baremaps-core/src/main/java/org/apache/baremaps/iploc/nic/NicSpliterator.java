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



import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/** A spliterator for Nic Objects. */
class NicSpliterator implements Spliterator<NicObject> {

  private final Spliterator<String> lineSpliterator;

  private String line;

  NicSpliterator(Spliterator<String> lineSpliterator) {
    this.lineSpliterator = lineSpliterator;
  }

  public long estimateSize() {
    return lineSpliterator.estimateSize();
  }

  public int characteristics() {
    return lineSpliterator.characteristics();
  }

  public Spliterator<NicObject> trySplit() {
    return null;
  }

  public boolean tryAdvance(Consumer<? super NicObject> consumer) {
    StringBuilder keyBuilder = new StringBuilder();
    StringBuilder valBuilder = new StringBuilder();
    List<NicAttribute> attributes = new ArrayList<>();

    boolean tryAdvance;
    while ((tryAdvance = lineSpliterator.tryAdvance(this::acceptLine)) && !"".equals(line)) {
      // handle multiline values
      if (line.startsWith(" ")) {
        valBuilder.append("\n");
        valBuilder.append(line.trim());
      }

      // handle multiline values
      else if (line.startsWith("+")) {
        valBuilder.append("\n");
        valBuilder.append(line.substring(1).trim());
      }

      // handle attribute line
      else if (!line.startsWith("#") && !line.startsWith("%")) {
        int index = line.indexOf(":");
        if (index >= 0) {
          addAttribute(attributes, keyBuilder, valBuilder);
          keyBuilder = new StringBuilder();
          valBuilder = new StringBuilder();
          keyBuilder.append(line.substring(0, index).trim());
          valBuilder.append(line.substring(index + 1).trim());
        }
      }
    }

    // handle last attribute
    addAttribute(attributes, keyBuilder, valBuilder);

    // build object
    if (!attributes.isEmpty()) {
      consumer.accept(new NicObject(attributes));
    }

    return tryAdvance;
  }

  private void acceptLine(String line) {
    this.line = line;
  }

  private void addAttribute(List<NicAttribute> attributes, StringBuilder key, StringBuilder val) {
    if (key.length() > 0) {
      attributes.add(new NicAttribute(key.toString(), val.toString()));
    }
  }
}
