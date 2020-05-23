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

package com.baremaps.util.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;

class TryTest {

  private final Object object = new Object();

  private final Exception exception = new Exception();

  private final Callable<Object> success = () -> object;

  private final Callable<Object> failure = () -> {
    throw exception;
  };

  @Test
  public void of() {
    assertTrue(Try.of(success) instanceof Try.Success);
    assertTrue(Try.of(failure) instanceof Try.Failure);
  }

  @Test
  public void isSuccess() {
    assertTrue(Try.of(success).isSuccess());
    assertFalse(Try.of(failure).isSuccess());
  }

  @Test
  public void value() {
    assertEquals(Try.of(success).value(), object);
    assertEquals(Try.of(failure).value(), null);
  }

  @Test
  public void exception() {
    assertEquals(Try.of(success).exception(), null);
    assertEquals(Try.of(failure).exception(), exception);
  }
}