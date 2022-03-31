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

package com.baremaps.core.postgres;

/**
 * This code has been adapted from {@link <a
 * href="https://github.com/PgBulkInsert/PgBulkInsert">PgBulkInsert</a>} licensed under the MIT
 * license.
 *
 * <p>Copyright (c) The PgBulkInsert Team.
 */
class ObjectIdentifier {

  private ObjectIdentifier() {}

  // region OID 1 - 99

  // boolean, 'true'/'false'
  public static final int BOOLEAN = 16;

  // variable-length string, binary values escaped
  public static final int BYTEA = 17;

  // single character
  public static final int CHAR = 18;

  // 63-byte type for storing system identifiers
  public static final int NAME = 19;

  // ~18 digit integer, 8-byte storage
  public static final int INT8 = 20;

  // -32 thousand to 32 thousand, 2-byte storage
  public static final int INT2 = 21;

  // -2 billion to 2 billion integer, 4-byte storage
  public static final int INT4 = 23;

  // variable-length string, no limit specified
  public static final int TEXT = 25;

  // object identifier(oid), maximum 4 billion
  public static final int OID = 26;

  // (block, offset), physical location of tuple
  public static final int TID = 27;

  // transaction id
  public static final int XID = 28;

  // command identifier type, sequence in transaction id
  public static final int CID = 29;

  // endregion

  // region OID 100 - 199

  // JSON
  public static final int JSONB = 114;

  // XML content
  public static final int XML = 115;

  // endregion

  // region OID 600 - 699

  // geometric toPoint '(x, y)'
  public static final int POINT = 600;

  // geometric line segment '(pt1, pt2)'
  public static final int LINE_SEGMENT = 601;

  // geometric path '(pt1,...)'
  public static final int PATH = 602;

  // geometric box '(lower left, upper right)'
  public static final int BOX = 603;

  // geometric polygon '(pt1, ...)'
  public static final int POLYGON = 604;

  // geometric line
  public static final int LINE = 628;

  // endregion

  // region OID 700 - 799

  // single-precision floating toPoint number, 4-byte storage
  public static final int SINGLE_PRECISION = 700;

  // double-precision floating toPoint number, 8-byte storage
  public static final int DOUBLE_PRECISION = 701;

  // absolute, limited-range date and time (Unix system time)
  public static final int ABS_TIME = 702;

  // relative, limited-range time interval (Unix delta time)
  public static final int REL_TIME = 703;

  // (abstime, abstime), time interval
  public static final int T_INTERVAL = 704;

  // unknown
  public static final int UNKNOWN = 705;

  // geometric circle '(center, radius)'
  public static final int CIRCLE = 705;

  // monetary amounts, $d,ddd.cc
  public static final int CASH = 790;

  // money
  public static final int MONEY = 791;

  // endregion

  // region OID 800 - 899

  // XX:XX:XX:XX:XX:XX, MAC address
  public static final int MAC_ADDRESS = 829;

  // IP address/netmask, host address, netmask optional
  public static final int INET = 869;

  // network IP address/netmask, network address
  public static final int CIDR = 650;

  // XX:XX:XX:XX:XX:XX:XX:XX, MAC address
  public static final int MAC_ADDRESS_8 = 774;

  // endregion

  // region OIDS 1000 - 1099

  // char(length), blank-padded string, fixed storage length
  public static final int CHAR_LENGTH = 1042;

  // varchar(length), non-blank-padded string, variable storage length
  public static final int VAR_CHAR_LENGTH = 1043;

  // Date
  public static final int DATE = 1082;

  // Time Of Day
  public static final int TIME = 1082;

  // endregion

  // region OIDS 1100 - 1199

  // date and time
  public static final int TIMESTAMP = 1114;

  // date and time with time zone
  public static final int TIMESTAMP_TZ = 1184;

  // Interval
  public static final int INTERVAL = 1186;

  // endregion

  // region OIDS 1200 - 1299

  // time of day with time zone
  public static final int TIME_TZ = 1266;

  // endregion

  // region OIDS 1500 - 1599

  // fixed-length bit string
  public static final int BIT = 1560;

  // variable-length bit string
  public static final int VAR_BIT = 1562;

  // endregion

  // region OIDS 1700 - 1799

  public static final int NUMERIC = 1700;

  // endregion

  // region UUID

  public static final int UUID = 2950;

  // endregion

  // region Pseudo-Types

  public static final int RECORD = 2249;
}
