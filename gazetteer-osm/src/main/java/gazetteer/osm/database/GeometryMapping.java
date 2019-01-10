package gazetteer.osm.database;

import de.bytefish.pgbulkinsert.mapping.AbstractMapping;
import de.bytefish.pgbulkinsert.pgsql.handlers.IValueHandlerProvider;
import mil.nga.sf.Geometry;

import java.util.function.Function;

public class GeometryMapping<TEntity> extends AbstractMapping<TEntity> {

    protected GeometryMapping(String schemaName, String tableName) {
        super(schemaName, tableName);
    }

    protected GeometryMapping(IValueHandlerProvider provider, String schemaName, String tableName) {
        super(provider, schemaName, tableName);
    }

    public void mapGeometry(String columnName, Function<TEntity, Geometry> propertyGetter) {
        map(columnName, new GeometryValueHandler(), propertyGetter);
    }

}
