package io.gazetteer.osm.postgis;

import com.google.common.base.Charsets;
import de.bytefish.pgbulkinsert.util.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HstoreUtil {

  public static byte[] asHstore(Map<String, String> value) {
    try {
      return write(value);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static byte[] write(Map<String, String> value) throws IOException {
    ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
    DataOutputStream hstoreOutput = new DataOutputStream(byteArrayOutput);
    hstoreOutput.writeInt(value.size());
    for (Map.Entry<String, String> entry : value.entrySet()) {
      writeKey(hstoreOutput, entry.getKey());
      writeValue(hstoreOutput, entry.getValue());
    }
    return byteArrayOutput.toByteArray();
  }

  private static void writeKey(DataOutputStream buffer, String key) throws IOException {
    writeText(buffer, key);
  }

  private static void writeValue(DataOutputStream buffer, String value) throws IOException {
    if (value == null) {
      buffer.writeInt(-1);
    } else {
      writeText(buffer, value);
    }
  }

  private static void writeText(DataOutputStream buffer, String text) throws IOException {
    byte[] textBytes = StringUtils.getUtf8Bytes(text);
    buffer.writeInt(textBytes.length);
    buffer.write(textBytes);
  }

  public static Map<String, String> asMap(byte[] hstore) {
    try {
      return read(hstore);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static Map<String, String> read(byte[] hstore) throws IOException {
    Map<String, String> entries = new HashMap<>();
    ByteArrayInputStream byteArrayInput = new ByteArrayInputStream(hstore);
    DataInputStream hstoreInput = new DataInputStream(byteArrayInput);
    int size = hstoreInput.readInt();
    for (int i = 0; i < size; i++) {
      String key = readText(hstoreInput);
      String val = readText(hstoreInput);
      entries.put(key, val);
    }
    return entries;
  }

  private static String readText(DataInputStream buffer) throws IOException {
    int length = buffer.readInt();
    if (length >= 0) {
      byte[] bytes = new byte[length];
      buffer.read(bytes);
      return new String(bytes, Charsets.UTF_8);
    } else {
      return null;
    }
  }
}
