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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;

/** Custom deserializer for RipeObject */
public class RipeObjectDeserializer extends JsonDeserializer<RipeObject> {

  /** {@inheritDoc} */
  @Override
  public RipeObject deserialize(
      JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    ObjectCodec oc = jsonParser.getCodec();
    JsonNode node = oc.readTree(jsonParser);

    ObjectMapper objectMapper = new ObjectMapper();
    List<RipeAttribute> attributes =
        objectMapper.readValue(
            node.toPrettyString(),
            objectMapper.getTypeFactory().constructCollectionType(List.class, RipeAttribute.class));
    return new RipeObject(attributes);
  }
}
