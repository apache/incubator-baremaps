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

package org.apache.baremaps.database.collection;


import java.util.Iterator;
import java.util.function.Function;

/**
 * A decorator for a table that transforms the geometries of the rows.
 */
public class DataCollectionAdapter<S, T> extends AbstractDataCollection<T> {

  private final DataCollection<S> collection;

  private final Function<S, T> transformer;

  /**
   * Constructs a new table decorator.
   *
   * @param collection the table to decorate
   * @param transformer the row transformer
   */
  public DataCollectionAdapter(DataCollection<S> collection, Function<S, T> transformer) {
    this.collection = collection;
    this.transformer = transformer;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator iterator() {
    return collection.stream().map(this.transformer).iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long sizeAsLong() {
    return collection.sizeAsLong();
  }
}
