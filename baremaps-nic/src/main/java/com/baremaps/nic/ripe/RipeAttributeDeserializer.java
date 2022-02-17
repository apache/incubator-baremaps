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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

/** Custom deserializer for RipeAttribute */
public class RipeAttributeDeserializer extends JsonDeserializer<RipeAttribute> {

  /** {@inheritDoc} */
  @Override
  public RipeAttribute deserialize(
      JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    ObjectCodec oc = jsonParser.getCodec();
    JsonNode node = oc.readTree(jsonParser);

    String attributeName = node.fieldNames().next();
    String attributeValue = node.get(attributeName).asText();

    return new RipeAttribute(attributeName, attributeValue);
  }
}
