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

package org.apache.baremaps.storage.postgres;



import java.util.Iterator;
import org.apache.baremaps.collection.AbstractDataCollection;
import org.apache.baremaps.dataframe.DataFrame;
import org.apache.baremaps.dataframe.DataType;
import org.apache.baremaps.dataframe.Row;

public class PostgresDataFrame extends AbstractDataCollection<Row> implements DataFrame {

  private final DataType dataType;

  public PostgresDataFrame(DataType dataType) {
    this.dataType = dataType;
  }

  @Override
  public Iterator<Row> iterator() {
    return null;
  }

  @Override
  public long sizeAsLong() {
    return 0;
  }

  @Override
  public DataType dataType() {
    return dataType();
  }
}
