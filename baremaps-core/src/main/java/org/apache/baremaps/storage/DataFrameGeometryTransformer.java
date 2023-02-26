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
import org.apache.baremaps.collection.AbstractDataCollection;
import org.apache.baremaps.dataframe.Column;
import org.apache.baremaps.dataframe.DataFrame;
import org.apache.baremaps.dataframe.Row;
import org.apache.baremaps.dataframe.Schema;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.GeometryTransformer;

public class DataFrameGeometryTransformer extends AbstractDataCollection<Row> implements DataFrame {

  private final DataFrame dataFrame;

  private final GeometryTransformer transformer;

  public DataFrameGeometryTransformer(DataFrame dataFrame,
      GeometryTransformer geometryTransformer) {
    this.dataFrame = dataFrame;
    this.transformer = geometryTransformer;
  }

  @Override
  public Schema schema() {
    return dataFrame.schema();
  }

  public Row transform(Row row) {
    var columns = schema().columns().stream()
        .filter(column -> column.type().isInstance(Geometry.class)).toList();
    for (Column column : columns) {
      var name = column.name();
      var geometry = (Geometry) row.get(name);
      row.set(name, transformer.transform(geometry));
    }
    return row;
  }

  @Override
  public Iterator<Row> iterator() {
    return dataFrame.stream().map(this::transform).iterator();
  }

  @Override
  public long sizeAsLong() {
    return dataFrame.sizeAsLong();
  }
}
