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

package org.apache.baremaps.database.collection;


import java.util.Iterator;
import java.util.function.Function;

/**
 * A decorator for a table that transforms the geometries of the rows.
 */
public class DataCollectionMapper<S, T> implements DataCollection<T> {

  private final DataCollection<S> collection;

  private final Function<S, T> mapper;

  /**
   * Constructs a new table decorator.
   *
   * @param collection the table to decorate
   * @param mapper the row transformer
   */
  public DataCollectionMapper(DataCollection<S> collection, Function<S, T> mapper) {
    this.collection = collection;
    this.mapper = mapper;
  }

  @Override
  public long size() {
    return collection.size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<T> iterator() {
    return collection.stream().map(this.mapper).iterator();
  }

  @Override
  public void clear() {
    collection.clear();
  }
}
