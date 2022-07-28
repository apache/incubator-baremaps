// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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