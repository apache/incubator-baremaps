package com.baremaps.postgres.jdbi;

import com.baremaps.osm.geometry.GeometryUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.locationtech.jts.geom.Geometry;

abstract class BaseColumnMapper<T extends Geometry> implements ColumnMapper<T> {

  @Override
  public T map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
    byte[] bytes = hexStringToByteArray(r.getString(columnNumber));
    return (T) GeometryUtils.deserialize(bytes);
  }

  private static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
    }
    return data;
  }

}
