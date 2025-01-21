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

package org.apache.baremaps.store;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.baremaps.store.DataColumn.Cardinality;
import org.apache.baremaps.store.DataColumn.ColumnType;

/**
 * A {@link DataSchema} is a description of the structure of a row in a {@link DataTable}.
 */
public interface DataSchema extends Serializable {

  /**
   * Returns the name of the schema.
   *
   * @return the name of the schema
   */
  String name();

  /**
   * Returns the columns of the schema.
   *
   * @return the columns of the schema
   */
  List<DataColumn> columns();

  /**
   * Creates a new row of the schema.
   *
   * @return a new row of the schema
   */
  DataRow createRow();

  class DataSchemaDeserializer extends JsonDeserializer<DataSchema> {

    @Override
    public DataSchema deserialize(JsonParser parser, DeserializationContext ctxt)
        throws IOException {
      ObjectNode node = parser.getCodec().readTree(parser);
      String name = node.get("name").asText();
      List<DataColumn> columns = new ArrayList<>();
      node.get("columns").elements().forEachRemaining(column -> {
        columns.add(deserialize(column));
      });
      return new DataSchemaImpl(name, columns);
    }

    DataColumn deserialize(JsonNode node) {
      String columnName = node.get("name").asText();
      Cardinality cardinality = Cardinality.valueOf(node.get("cardinality").asText());
      ColumnType type = ColumnType.valueOf(node.get("type").asText());
      if (type == ColumnType.NESTED) {
        List<DataColumn> columns = new ArrayList<>();
        node.get("columns").elements().forEachRemaining(column -> {
          columns.add(deserialize(column));
        });
        return new DataColumnNested(columnName, cardinality, columns);
      } else {
        return new DataColumnFixed(columnName, cardinality, type);
      }
    }
  }

  private static ObjectMapper configureObjectMapper() {
    var mapper = new ObjectMapper();
    mapper.registerSubtypes(
        new NamedType(DataColumnFixed.class, "FIXED"),
        new NamedType(DataColumnNested.class, "NESTED"));
    var module = new SimpleModule();
    module.addDeserializer(DataSchema.class, new DataSchemaDeserializer());
    mapper.registerModule(module);
    return mapper;
  }

  static DataSchema read(Path path) throws IOException {
    var mapper = configureObjectMapper();
    return mapper.readValue(path.toFile(), DataSchema.class);
  }

  static void write(Path path, DataSchema schema) throws IOException {
    var mapper = configureObjectMapper();
    mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), schema);
  }
  
}
