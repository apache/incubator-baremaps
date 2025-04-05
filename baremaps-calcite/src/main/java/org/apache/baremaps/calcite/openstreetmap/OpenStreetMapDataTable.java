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

package org.apache.baremaps.calcite.openstreetmap;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;
import org.apache.baremaps.calcite.*;
import org.apache.baremaps.calcite.DataColumn.Type;
import org.apache.baremaps.openstreetmap.OpenStreetMapFormat.EntityReader;
import org.apache.baremaps.openstreetmap.model.*;

/**
 * A DataTable implementation for OpenStreetMap data.
 */
public class OpenStreetMapDataTable implements DataTable {

  private final DataSchema schema;
  private final EntityReader<Entity> entityReader;
  private final InputStream inputStream;

  /**
   * Constructs an OpenStreetMapDataTable with the specified parameters.
   *
   * @param entityReader the EntityReader
   * @param inputStream the input stream
   */
  public OpenStreetMapDataTable(EntityReader<Entity> entityReader, InputStream inputStream) {
    this.entityReader = entityReader;
    this.inputStream = inputStream;
    this.schema = createSchema();
  }

  /**
   * Creates the schema for the OpenStreetMap data.
   *
   * @return the DataSchema
   */
  private DataSchema createSchema() {
    List<DataColumn> columns = List.of(
        new DataColumnFixed("id", DataColumn.Cardinality.REQUIRED, Type.LONG),
        new DataColumnFixed("type", DataColumn.Cardinality.REQUIRED, Type.STRING),
        new DataColumnFixed("version", DataColumn.Cardinality.OPTIONAL, Type.INTEGER),
        new DataColumnFixed("timestamp", DataColumn.Cardinality.OPTIONAL,
            Type.LOCAL_DATE_TIME),
        new DataColumnFixed("uid", DataColumn.Cardinality.OPTIONAL, Type.INTEGER),
        new DataColumnFixed("user", DataColumn.Cardinality.OPTIONAL, Type.STRING),
        new DataColumnFixed("changeset", DataColumn.Cardinality.OPTIONAL, Type.LONG),
        new DataColumnFixed("tags", DataColumn.Cardinality.OPTIONAL, Type.NESTED),
        new DataColumnFixed("geometry", DataColumn.Cardinality.OPTIONAL,
            Type.GEOMETRY));
    return new DataSchema("osm_data", columns);
  }

  @Override
  public DataSchema schema() {
    return schema;
  }

  @Override
  public boolean add(DataRow row) {
    throw new UnsupportedOperationException(
        "Add operation is not supported for OpenStreetMapDataTable.");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException(
        "Clear operation is not supported for OpenStreetMapDataTable.");
  }

  @Override
  public long size() {
    // Unknown size
    return Long.MAX_VALUE;
  }

  @Override
  public Iterator<DataRow> iterator() {
    Stream<Element> elementStream = entityReader.read(inputStream)
        .filter(element -> element instanceof Element)
        .map(element -> (Element) element);
    return elementStream.map(this::elementToDataRow).iterator();
  }

  /**
   * Converts an Element to a DataRow.
   *
   * @param element the OSM Element
   * @return the corresponding DataRow
   */
  private DataRow elementToDataRow(Element element) {
    DataRow row = schema.createRow();
    row.set("id", element.getId());
    row.set("type", elementTypeToString(element));
    row.set("version", element.getInfo().getVersion());
    row.set("timestamp", element.getInfo().getTimestamp());
    row.set("uid", element.getInfo().getUid());
    row.set("changeset", element.getInfo().getChangeset());
    row.set("tags", element.getTags());
    row.set("geometry", element.getGeometry());
    return row;
  }

  /**
   * Converts the element type to a String.
   *
   * @param element the OSM Element
   * @return the element type as String
   */
  private String elementTypeToString(Element element) {
    if (element instanceof Node)
      return "node";
    else if (element instanceof Way)
      return "way";
    else if (element instanceof Relation)
      return "relation";
    else
      return "unknown";
  }

  @Override
  public void close() throws Exception {
    inputStream.close();
  }
}
