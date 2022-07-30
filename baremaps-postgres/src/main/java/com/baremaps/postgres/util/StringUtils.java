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

package com.baremaps.postgres.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StringUtils {

  private static Charset utf8Charset = StandardCharsets.UTF_8;

  private StringUtils() {}

  public static boolean isNullOrWhiteSpace(@Nullable String input) {
    return input == null || input.trim().length() == 0;
  }

  public static byte[] getUtf8Bytes(String value) {
    return value.getBytes(utf8Charset);
  }

  @Nullable
  public static String removeNullCharacter(@Nullable String data) {
    if (data == null) {
      return data;
    }

    return data.replaceAll("\u0000", "");
  }
}
