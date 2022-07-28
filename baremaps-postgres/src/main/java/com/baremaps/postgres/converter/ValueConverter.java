// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.converter;

public interface ValueConverter<S, T> {

    T convert(S source);

}
