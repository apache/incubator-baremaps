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

package org.apache.baremaps.storage;


import java.util.Iterator;
import java.util.function.Function;
import org.apache.baremaps.collection.AbstractDataCollection;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.GeometryTransformer;

/**
 * A decorator for a table that transforms the geometries of the rows.
 */
public class TableDecorator extends AbstractDataCollection<Row> implements Table {

  private final Table table;

  private final Function<Row, Row> transformer;

  /**
   * Constructs a new table geometry decorator.
   *
   * @param table the table to decorate
   * @param geometryTransformer the geometry transformer
   */
  public TableDecorator(Table table, GeometryTransformer geometryTransformer) {
    this(table, row -> {
      var columns = table.schema()
          .columns().stream()
          .filter(column -> column.type().isAssignableFrom(Geometry.class))
          .toList();
      for (Column column : columns) {
        var name = column.name();
        var geometry = (Geometry) row.get(name);
        if (geometry != null) {
          row.set(name, geometryTransformer.transform(geometry));
        }
      }
      return row;
    });
  }

  /**
   * Constructs a new table decorator.
   *
   * @param table the table to decorate
   * @param transformer the row transformer
   */
  public TableDecorator(Table table, Function<Row, Row> transformer) {
    this.table = table;
    this.transformer = transformer;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Schema schema() {
    return table.schema();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<Row> iterator() {
    return table.stream().map(this.transformer).iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long sizeAsLong() {
    return table.sizeAsLong();
  }
}
