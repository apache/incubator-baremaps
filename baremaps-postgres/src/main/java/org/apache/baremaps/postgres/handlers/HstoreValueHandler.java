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

package org.apache.baremaps.postgres.handlers;



import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import org.apache.baremaps.postgres.util.StringUtils;

public class HstoreValueHandler extends BaseValueHandler<Map<String, String>> {

  @Override
  protected void internalHandle(DataOutputStream buffer, final Map<String, String> value)
      throws IOException {

    // Write into a Temporary ByteArrayOutputStream:
    ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();

    // And wrap it in a DataOutputStream:
    DataOutputStream hstoreOutput = new DataOutputStream(byteArrayOutput);

    // First the Amount of Values to write:
    hstoreOutput.writeInt(value.size());

    // Now Iterate over the Array and write each value:
    for (Map.Entry<String, String> entry : value.entrySet()) {
      // Write the Key:
      writeKey(hstoreOutput, entry.getKey());
      // The Value can be null, use a different method:
      writeValue(hstoreOutput, entry.getValue());
    }

    // Now write the entire ByteArray to the COPY Buffer:
    buffer.writeInt(byteArrayOutput.size());
    buffer.write(byteArrayOutput.toByteArray());
  }

  private void writeKey(DataOutputStream buffer, String key) throws IOException {
    writeText(buffer, key);
  }

  private void writeValue(DataOutputStream buffer, String value) throws IOException {
    if (value == null) {
      buffer.writeInt(-1);
    } else {
      writeText(buffer, value);
    }
  }

  private void writeText(DataOutputStream buffer, String text) throws IOException {
    byte[] textBytes = StringUtils.getUtf8Bytes(text);
    buffer.writeInt(textBytes.length);
    buffer.write(textBytes);
  }
}
