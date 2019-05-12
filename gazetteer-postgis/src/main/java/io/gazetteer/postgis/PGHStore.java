/*
 * This file has been copied from the following location:
 * http://archives.postgresql.org/pgsql-jdbc/2009-12/msg00037.php
 *
 * PostgreSQL code is typically under a BSD licence.
 * http://jdbc.postgresql.org/license.html
 */

/*-------------------------------------------------------------------------
 *
 * A preliminary version of a custom type wrapper for hstore data.
 * Once it gets some testing and cleanups it will go into the official
 * PG JDBC driver, but stick it here for now because we need it sooner.
 *
 * Copyright (c) 2009, PostgreSQL Global Development Group
 *
 * IDENTIFICATION
 *   $PostgreSQL$
 *
 *-------------------------------------------------------------------------
 */
package io.gazetteer.postgis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.postgresql.util.PGobject;

public class PGHStore extends PGobject {

  private Map<String, String> map;

  public PGHStore() {
    setType("hstore");
  }

  public Map<String, String> getMap() {
    return map;
  }

  public void setMap(Map<String, String> map) {
    this.map = map;
  }

  public void setValue(String value) throws SQLException {
    Parser p = new Parser();
    map = p.parse(value);
  }

  public String getValue() {
    StringBuilder builder = new StringBuilder();
    Iterator<String> i = map.keySet().iterator();
    boolean first = true;
    while (i.hasNext()) {
      Object key = i.next();
      Object value = map.get(key);

      if (first) {
        first = false;
      } else {
        builder.append(',');
      }

      writeValue(builder, key);
      builder.append("=>");
      writeValue(builder, value);
    }

    return builder.toString();
  }

  private static void writeValue(StringBuilder buf, Object o) {
    if (o == null) {
      buf.append("NULL");
      return;
    }

    String s = o.toString();

    buf.append('"');
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '"' || c == '\\') {
        buf.append('\\');
      }
      buf.append(c);
    }
    buf.append('"');
  }

  /**
   * Returns whether an object is equal to this one or not
   *
   * @param obj Object to compare with
   * @return true if the two hstores are identical
   */
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof PGHStore)) {
      return false;
    }
    return map.equals(((PGHStore) obj).map);

  }

  private static class Parser {

    private String value;
    private int ptr;
    private StringBuilder cur;
    private boolean escaped;

    private List<String> keys;
    private List<String> values;

    private final static int GV_WAITVAL = 0;
    private final static int GV_INVAL = 1;
    private final static int GV_INESCVAL = 2;
    private final static int GV_WAITESCIN = 3;
    private final static int GV_WAITESCESCIN = 4;

    private final static int WKEY = 0;
    private final static int WVAL = 1;
    private final static int WEQ = 2;
    private final static int WGT = 3;
    private final static int WDEL = 4;

    private Map<String, String> parse(String value) throws SQLException {
      this.value = value;
      ptr = 0;
      keys = new ArrayList<>();
      values = new ArrayList<>();

      parseHStore();

      Map<String, String> map = new HashMap<>();
      for (int i = 0; i < keys.size(); i++) {
        map.put(keys.get(i), values.get(i));
      }

      return map;
    }

    private boolean getValue(boolean ignoreEqual) throws SQLException {
      int state = GV_WAITVAL;

      cur = new StringBuilder();
      escaped = false;

      while (true) {
        boolean atEnd = (value.length() == ptr);
        char c = '\0';
        if (!atEnd) {
          c = value.charAt(ptr);
        }

        if (state == GV_WAITVAL) {
          if (c == '"') {
            escaped = true;
            state = GV_INESCVAL;
          } else if (c == '\0') {
            return false;
          } else if (c == '=' && !ignoreEqual) {
            throw new SQLException("KJJ");
          } else if (c == '\\') {
            state = GV_WAITESCIN;
          } else if (!Character.isWhitespace(c)) {
            cur.append(c);
            state = GV_INVAL;
          }
        } else if (state == GV_INVAL) {
          if (c == '\\') {
            state = GV_WAITESCIN;
          } else if (c == '=' && !ignoreEqual) {
            ptr--;
            return true;
          } else if (c == ',' && ignoreEqual) {
            ptr--;
            return true;
          } else if (Character.isWhitespace(c)) {
            return true;
          } else if (c == '\0') {
            ptr--;
            return true;
          } else {
            cur.append(c);
          }
        } else if (state == GV_INESCVAL) {
          if (c == '\\') {
            state = GV_WAITESCESCIN;
          } else if (c == '"') {
            return true;
          } else if (c == '\0') {
            throw new SQLException("KJJ, unexpected end of string");
          } else {
            cur.append(c);
          }
        } else if (state == GV_WAITESCIN) {
          if (c == '\0') {
            throw new SQLException("KJJ, unexpected end of string");
          }

          cur.append(c);
          state = GV_INVAL;
        } else if (state == GV_WAITESCESCIN) {
          if (c == '\0') {
            throw new SQLException("KJJ, unexpected end of string");
          }

          cur.append(c);
          state = GV_INESCVAL;
        } else {
          throw new SQLException("KJJ");
        }

        ptr++;
      }
    }

    private void parseHStore() throws SQLException {
      int state = WKEY;
      escaped = false;

      while (true) {
        char c = '\0';
        if (ptr < value.length()) {
          c = value.charAt(ptr);
        }

        if (state == WKEY) {
          if (!getValue(false)) {
            return;
          }

          keys.add(cur.toString());
          cur = null;
          state = WEQ;
        } else if (state == WEQ) {
          if (c == '=') {
            state = WGT;
          } else if (state == '\0') {
            throw new SQLException("KJJ, unexpected end of string");
          } else if (!Character.isWhitespace(c)) {
            throw new SQLException("KJJ, syntax err");
          }
        } else if (state == WGT) {
          if (c == '>') {
            state = WVAL;
          } else if (c == '\0') {
            throw new SQLException("KJJ, unexpected end of string");
          } else {
            throw new SQLException("KJJ, syntax err [" + c + "] at " + ptr);
          }
        } else if (state == WVAL) {
          if (!getValue(true)) {
            throw new SQLException("KJJ, unexpected end of string");
          }

          String val = cur.toString();
          cur = null;
          if (!escaped && "null".equalsIgnoreCase(val)) {
            val = null;
          }

          values.add(val);
          state = WDEL;
        } else if (state == WDEL) {
          if (c == ',') {
            state = WKEY;
          } else if (c == '\0') {
            return;
          } else if (!Character.isWhitespace(c)) {
            throw new SQLException("KJJ, syntax err");
          }
        } else {
          throw new SQLException("KJJ unknown state");
        }

        ptr++;
      }
    }

  }
}
