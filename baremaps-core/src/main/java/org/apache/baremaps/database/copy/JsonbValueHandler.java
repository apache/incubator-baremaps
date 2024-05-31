/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.database.copy;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bytefish.pgbulkinsert.pgsql.handlers.BaseValueHandler;
import java.io.DataOutputStream;

public class JsonbValueHandler extends BaseValueHandler<Object> {

  private final int jsonbProtocolVersion;

  public JsonbValueHandler() {
    this(1);
  }

  public JsonbValueHandler(int jsonbProtocolVersion) {
    this.jsonbProtocolVersion = jsonbProtocolVersion;
  }

  private static byte[] asJson(Object object) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      String value = objectMapper.writeValueAsString(object);
      return value.getBytes("UTF-8");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void internalHandle(DataOutputStream buffer, Object value) throws Exception {
    byte[] utf8Bytes = asJson(value);
    buffer.writeInt(utf8Bytes.length + 1);
    buffer.writeByte(jsonbProtocolVersion);
    buffer.write(utf8Bytes);
  }


  @Override
  public int getLength(Object value) {
    byte[] utf8Bytes = asJson(value);
    return utf8Bytes.length;
  }
}
