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
package com.baremaps.nic.ripe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Utils for RIPE datas */
public class RipeUtils {

  private static final Logger logger = LogManager.getLogger(RipeUtils.class);

  private RipeUtils() {}

  /**
   * Convert an input stream into a RipeObject Stream
   *
   * @param inputStream the stream of text input
   * @return Stream of Ripe Object
   */
  public static Stream<RipeObject> parse(InputStream inputStream) {
    return StreamSupport.stream(new RipeSpliterator(inputStream), false);
  }

  /**
   * Search if a ripe object has an attribute value
   *
   * @param ripeObject the object to verify
   * @param value the value to search for
   * @return true if the value exist in the object. False otherwise
   */
  public static boolean hasAttributeValue(RipeObject ripeObject, String value) {
    return ripeObject.attributes().stream()
        .map(RipeAttribute::value)
        .anyMatch(v -> v.toLowerCase().contains(value.toLowerCase()));
  }
}
