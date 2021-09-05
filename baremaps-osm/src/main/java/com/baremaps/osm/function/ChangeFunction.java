/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.osm.function;

import com.baremaps.osm.domain.Change;
import com.baremaps.stream.StreamException;
import java.util.function.Function;

/**
 * Represents a function that transforms entities of different types.
 *
 * @param <T>
 */
public interface ChangeFunction<T> extends Function<Change, T> {

  /** {@inheritDoc} */
  @Override
  default T apply(Change change) {
    try {
      return change.visit(this);
    } catch (StreamException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  /**
   * Applies a function on a {@code Change}.
   *
   * @param change the change
   * @return the function result
   * @throws Exception
   */
  T match(Change change) throws Exception;
}
