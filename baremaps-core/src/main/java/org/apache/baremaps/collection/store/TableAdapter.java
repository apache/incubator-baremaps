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

package org.apache.baremaps.collection.store;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.function.Function;

public class TableAdapter<T> extends AbstractCollection<T> {

  private final Function<Row, T> transformer;

  private final Table table;

  public TableAdapter(Table table, Function<Row, T> transformer) {
    this.transformer = transformer;
    this.table = table;
  }

  @Override
  public Iterator iterator() {
    return new TableIterator(table.iterator());
  }

  @Override
  public int size() {
    return table.size();
  }

  private class TableIterator implements Iterator<T> {

    private final Iterator<Row> iterator;

    public TableIterator(Iterator<Row> iterator) {
      this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public T next() {
      var row = iterator.next();
      return transformer.apply(row);
    }
  }

}
