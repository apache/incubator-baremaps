package com.baremaps.postgres.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class FeatureMapper implements RowMapper<Feature> {

  private final GeometryColumnMapper mapper = new GeometryColumnMapper();

  @Override
  public Feature map(ResultSet rs, StatementContext ctx) throws SQLException {
    var feature = new Feature();
    feature.setProperties(new HashMap<>());

    var metadata = ctx.getStatement().getMetaData();
    var columnCount = metadata.getColumnCount();
    for (int i = 1; i <= columnCount; i++) {
      var columnName = metadata.getColumnName(i);
      switch (columnName) {
        case "id":
        case "fid":
        case "gid":
          // Set first occurrence as id
          if (feature.getId() == null) {
            feature.setId(rs.getString(i));
          }
          // Set following occurrence as properties
          else {
            feature.getProperties().put(columnName, rs.getString(i));
          }
          break;
        case "type":
          feature.setType(rs.getString(i));
          break;
        default:
          var columnTypeName = metadata.getColumnTypeName(i);
          switch (columnTypeName) {
            case "geometry":
            case "geometry(geometry)":
            case "geometry(point)":
            case "geometry(linestring)":
            case "geometry(polygon)":
              // Set first occurrences as geometry and ignore remaining ones
              if (feature.getGeometry() == null) {
                feature.setGeometry(mapper.map(rs, i, ctx));
              }
              break;
            default:
              feature.getProperties().put(columnName, rs.getString(i));
          }
      }
    }

    return feature;
  }
}
