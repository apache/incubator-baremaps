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

package org.apache.baremaps.stream;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A consumer that conditionally accepts an object.
 *
 * @param <T> the type of the input to the operation
 */
public abstract class ConditionalConsumer<T> implements Consumer<T> {

  private final Predicate<T> predicate;

  /**
   * Constructs a consumer that conditionally accepts an object.
   */
  protected ConditionalConsumer() {
    this.predicate = t -> true;
  }

  /**
   * Constructs a consumer that conditionally accepts an object.
   *
   * @param predicate the predicate
   */
  protected ConditionalConsumer(Predicate<T> predicate) {
    this.predicate = predicate;
  }

  /** {@inheritDoc} */
  @Override
  public void accept(T t) {
    if (predicate.test(t)) {
      conditionalAccept(t);
    }
  }

  /**
   * Conditionally accepts an object.
   *
   * @param t the object to be conditionally accepted
   */
  public abstract void conditionalAccept(T t);

}
