/*
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

package com.baremaps.postgres;

public enum DataType {
  UNSPECIFIED(0),
  INT2(21),
  INT2_ARRAY(1005),
  INT4(23),
  INT4_ARRAY(1007),
  INT8(20),
  INT8_ARRAY(1016),
  TEXT(25),
  TEXT_ARRAY(1009),
  NUMERIC(1700),
  NUMERIC_ARRAY(1231),
  FLOAT4(700),
  FLOAT4_ARRAY(1021),
  FLOAT8(701),
  FLOAT8_ARRAY(1022),
  BOOL(16),
  BOOL_ARRAY(1000),
  DATE(1082),
  DATE_ARRAY(1182),
  TIME(1083),
  TIME_ARRAY(1183),
  TIMETZ(1266),
  TIMETZ_ARRAY(1270),
  TIMESTAMP(1114),
  TIMESTAMP_ARRAY(1115),
  TIMESTAMPTZ(1184),
  TIMESTAMPTZ_ARRAY(1185),
  BYTEA(17),
  BYTEA_ARRAY(1001),
  VARCHAR(1043),
  VARCHAR_ARRAY(1015),
  OID(26),
  OID_ARRAY(1028),
  BPCHAR(1042),
  BPCHAR_ARRAY(1014),
  MONEY(790),
  MONEY_ARRAY(791),
  NAME(19),
  NAME_ARRAY(1003),
  BIT(1560),
  BIT_ARRAY(1561),
  VOID(2278),
  INTERVAL(1186),
  INTERVAL_ARRAY(1187),
  CHAR(18), // This is not char(N)), this is "char" a single byte type.
  CHAR_ARRAY(1002),
  VARBIT(1562),
  VARBIT_ARRAY(1563),
  UUID(2950),
  UUID_ARRAY(2951),
  XML(142),
  XML_ARRAY(143),
  POINT(600),
  POINT_ARRAY(1017),
  BOX(603),
  JSONB(3802),
  JSONB_ARRAY(3807),
  JSON(114),
  JSON_ARRAY(199),
  REF_CURSOR(1790),
  REF_CURSOR_ARRAY(2201),
  LINE(628),
  LSEG(601),
  PATH(602),
  POLYGON(604),
  CIRCLE(718),
  CIDR(650),
  INET(869),
  MACADDR(829),
  MACADDR8(774),
  TSVECTOR(3614),
  TSQUERY(3615);

  Integer dataType;
  Class javaType;

  DataType(Integer dataType) {
    this.dataType = dataType;
  }
}
