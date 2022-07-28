// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import java.io.IOException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.DataOutputStream;

public abstract class BaseValueHandler<T> implements ValueHandler<T> {

  @Override
  public void handle(DataOutputStream buffer, @Nullable final T value) throws IOException {
    if (value == null) {
      buffer.writeInt(-1);
      return;
    }
    internalHandle(buffer, value);
  }

  protected abstract void internalHandle(DataOutputStream buffer, final T value) throws IOException;
}
