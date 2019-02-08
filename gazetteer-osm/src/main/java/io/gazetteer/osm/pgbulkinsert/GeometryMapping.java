package io.gazetteer.osm.pgbulkinsert;

import de.bytefish.pgbulkinsert.mapping.AbstractMapping;
import de.bytefish.pgbulkinsert.pgsql.handlers.IValueHandlerProvider;
import de.bytefish.pgbulkinsert.pgsql.handlers.ValueHandlerProvider;
import org.locationtech.jts.geom.Geometry;

import java.util.function.Function;

public class GeometryMapping<T> extends AbstractMapping<T> {

  protected GeometryMapping(String schemaName, String tableName) {
    this(new ValueHandlerProvider(), schemaName, tableName);
  }

  protected GeometryMapping(IValueHandlerProvider provider, String schemaName, String tableName) {
    super(provider, schemaName, tableName);
  }

  public void mapGeometry(String columnName, Function<T, Geometry> propertyGetter) {
    map(columnName, new GeometryValueHandler(), propertyGetter);
  }
}
