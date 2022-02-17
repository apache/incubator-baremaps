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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/** Custom Serializer for RipeAttribute */
public class RipeAttributeSerializer extends JsonSerializer<RipeAttribute> {

  /** {@inheritDoc} */
  @Override
  public void serialize(
      RipeAttribute ripeAttribute,
      JsonGenerator jsonGenerator,
      SerializerProvider serializerProvider)
      throws IOException {
    jsonGenerator.writeStartObject();
    jsonGenerator.writeFieldName(ripeAttribute.getName());
    jsonGenerator.writeString(ripeAttribute.getValue());
    jsonGenerator.writeEndObject();
  }
}
