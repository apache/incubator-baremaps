// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license.

package io.gazetteer.osm.database;

public class ObjectIdentifier {

  // region OID 1 - 99

  // boolean, 'true'/'false'
  public static final int Boolean = 16;

  // variable-length string, binary values escaped
  public static final int Bytea = 17;

  // single character
  public static final int Char = 18;

  // 63-byte type for storing system identifiers
  public static final int Name = 19;

  // ~18 digit integer, 8-byte storage
  public static final int Int8 = 20;

  // -32 thousand to 32 thousand, 2-byte storage
  public static final int Int2 = 21;

  // -2 billion to 2 billion integer, 4-byte storage
  public static final int Int4 = 23;

  // variable-length string, no limit specified
  public static final int Text = 25;

  // object identifier(oid), maximum 4 billion
  public static final int Oid = 26;

  // (block, offset), physical location of tuple
  public static final int Tid = 27;

  // transaction id
  public static final int Xid = 28;

  // command identifier type, sequence in transaction id
  public static final int Cid = 29;

  // endregion

  // region OID 100 - 199

  // JSON
  public static final int Jsonb = 114;

  // XML content
  public static final int Xml = 115;

  // endregion

  // region OID 600 - 699

  // geometric toPoint '(x, y)'
  public static final int Point = 600;

  // geometric line segment '(pt1, pt2)'
  public static final int LineSegment = 601;

  // geometric path '(pt1,...)'
  public static final int Path = 602;

  // geometric box '(lower left, upper right)'
  public static final int Box = 603;

  // geometric polygon '(pt1, ...)'
  public static final int Polygon = 604;

  // geometric line
  public static final int Line = 628;

  // endregion

  // region OID 700 - 799

  // single-precision floating toPoint number, 4-byte storage
  public static final int SinglePrecision = 700;

  // double-precision floating toPoint number, 8-byte storage
  public static final int DoublePrecision = 701;

  // absolute, limited-range date and time (Unix system time)
  public static final int AbsTime = 702;

  // relative, limited-range time interval (Unix delta time)
  public static final int RelTime = 703;

  // (abstime, abstime), time interval
  public static final int TInterval = 704;

  // unknown
  public static final int Unknown = 705;

  // geometric circle '(center, radius)'
  public static final int Circle = 705;

  // monetary amounts, $d,ddd.cc
  public static final int Cash = 790;

  // money
  public static final int Money = 791;

  // endregion

  // region OID 800 - 899

  // XX:XX:XX:XX:XX:XX, MAC address
  public static final int MacAddress = 829;

  // IP address/netmask, host address, netmask optional
  public static final int Inet = 869;

  // network IP address/netmask, network address
  public static final int Cidr = 650;

  // XX:XX:XX:XX:XX:XX:XX:XX, MAC address
  public static final int MacAddress8 = 774;

  // endregion

  // region OIDS 1000 - 1099

  // char(length), blank-padded string, fixed storage length
  public static final int CharLength = 1042;

  // varchar(length), non-blank-padded string, variable storage length
  public static final int VarCharLength = 1043;

  // Date
  public static final int Date = 1082;

  // Time Of Day
  public static final int Time = 1082;

  // endregion

  // region OIDS 1100 - 1199

  // date and time
  public static final int Timestamp = 1114;

  // date and time with time zone
  public static final int TimestampTz = 1184;

  // Interval
  public static final int Interval = 1186;

  // endregion

  // region OIDS 1200 - 1299

  // time of day with time zone
  public static final int TimeTz = 1266;

  // endregion

  // region OIDS 1500 - 1599

  // fixed-length bit string
  public static final int Bit = 1560;

  // variable-length bit string
  public static final int VarBit = 1562;

  // endregion

  // region OIDS 1700 - 1799

  public static final int Numeric = 1700;

  // endregion

  // region UUID

  public static final int Uuid = 2950;

  // endregion

  // region Pseudo-Types

  public static final int Record = 2249;

}
