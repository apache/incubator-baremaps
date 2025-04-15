package org.apache.baremaps.calcite.postgres;

import org.apache.baremaps.postgres.metadata.DatabaseMetadata;
import org.apache.baremaps.postgres.metadata.TableMetadata;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostgresSchema extends AbstractSchema {

    private final DataSource dataSource;
    private final String schemaName;
    private final RelDataTypeFactory typeFactory;

    public PostgresSchema(DataSource dataSource, String schemaName, RelDataTypeFactory typeFactory) {
        this.dataSource = dataSource;
        this.schemaName = schemaName;
        this.typeFactory = typeFactory;
    }

    @Override
    protected Map<String, Table> getTableMap() {
        DatabaseMetadata databaseMetadata = new DatabaseMetadata(dataSource);
        Map<String, Table> tableMap = new HashMap<>();
        try {
            List<TableMetadata> tables = databaseMetadata.getTableMetaData(null, schemaName, null, new String[]{"TABLE"});
            for (TableMetadata table : tables) {
                String tableName = table.table().tableName();
                Table calciteTable = new PostgresModifiableTable(dataSource, tableName, typeFactory);
                tableMap.put(tableName, calciteTable);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tableMap;
    }
}
