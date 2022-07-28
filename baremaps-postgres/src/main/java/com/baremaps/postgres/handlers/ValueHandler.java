// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import java.io.IOException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.DataOutputStream;

@FunctionalInterface
public interface ValueHandler<T> {

    void handle(DataOutputStream buffer, @Nullable final T value) throws IOException;
}
