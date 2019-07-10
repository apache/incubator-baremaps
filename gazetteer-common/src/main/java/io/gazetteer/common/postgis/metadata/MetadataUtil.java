package io.gazetteer.common.postgis.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MetadataUtil {

  public static List<Table> getTables(
      Connection connection,
      String catalog,
      String schemaPattern,
      String tablePattern)
      throws SQLException {
    DatabaseMetaData metadata = connection.getMetaData();
    ResultSet results = metadata.getTables(catalog, schemaPattern, tablePattern, new String[]{"TABLE"});
    List<Table> tables = new ArrayList<>();
    while (results.next()) {
      tables.add(
          new Table(
              results.getString("TABLE_CAT"),
              results.getString("TABLE_SCHEM"),
              results.getString("TABLE_NAME"),
              results.getString("TABLE_TYPE"),
              results.getString("REMARKS")));
    }
    return tables;
  }

  public static List<TableColumn> getTableColumns(
      Connection connection,
      String catalog,
      String schemaPattern,
      String tablePattern,
      String columnPattern)
      throws SQLException {
    DatabaseMetaData metadata = connection.getMetaData();
    ResultSet results = metadata.getColumns(catalog, schemaPattern, tablePattern, columnPattern);
    List<TableColumn> columns = new ArrayList<>();
    while (results.next()) {
      columns.add(
          new TableColumn(
              results.getString("TABLE_CAT"),
              results.getString("TABLE_SCHEM"),
              results.getString("TABLE_NAME"),
              results.getString("COLUMN_NAME"),
              results.getInt("DATA_TYPE"),
              results.getString("TYPE_NAME"),
              results.getInt("COLUMN_SIZE"),
              results.getInt("BUFFER_LENGTH"),
              results.getInt("DECIMAL_DIGITS"),
              results.getInt("NUM_PREC_RADIX"),
              results.getInt("NULLABLE"),
              results.getString("REMARKS"),
              results.getString("COLUMN_DEF"),
              results.getInt("SQL_DATA_TYPE"),
              results.getInt("SQL_DATETIME_SUB"),
              results.getInt("CHAR_OCTET_LENGTH"),
              results.getInt("ORDINAL_POSITION"),
              results.getString("IS_NULLABLE"),
              results.getString("SCOPE_SCHEMA"),
              results.getString("SCOPE_TABLE"),
              results.getShort("SOURCE_DATA_TYPE"),
              results.getString("IS_AUTOINCREMENT")));
    }
    return columns;
  }

  public static List<PrimaryKeyColumn> getPrimaryKeyColumns(
      Connection connection, String catalog, String schema, String table) throws SQLException {
    DatabaseMetaData metadata = connection.getMetaData();
    ResultSet results = metadata.getPrimaryKeys(catalog, schema, table);
    List<PrimaryKeyColumn> columns = new ArrayList<>();
    while (results.next()) {
      columns.add(
          new PrimaryKeyColumn(
              results.getString("TABLE_CAT"),
              results.getString("TABLE_SCHEM"),
              results.getString("TABLE_NAME"),
              results.getString("COLUMN_NAME"),
              results.getShort("KEY_SEQ"),
              results.getString("PK_NAME")));
    }
    return columns;
  }

  public static List<Function> getFunctions(
      Connection connection, String catalog, String schemaPattern, String functionPattern)
      throws SQLException {
    DatabaseMetaData metadata = connection.getMetaData();
    ResultSet results = metadata.getFunctions(catalog, schemaPattern, functionPattern);
    List<Function> functions = new ArrayList<>();
    while (results.next()) {
      functions.add(
          new Function(
              results.getString("FUNCTION_CAT"),
              results.getString("FUNCTION_SCHEM"),
              results.getString("FUNCTION_NAME"),
              results.getString("REMARKS"),
              results.getShort("FUNCTION_TYPE"),
              results.getString("SPECIFIC_NAME")));
    }
    return functions;
  }

  public static List<FunctionColumn> getFunctionColumns(
      Connection connection,
      String catalog,
      String schemaPattern,
      String functionPattern,
      String columnPattern)
      throws SQLException {
    DatabaseMetaData metadata = connection.getMetaData();
    ResultSet results =
        metadata.getFunctionColumns(catalog, schemaPattern, functionPattern, columnPattern);
    List<FunctionColumn> columns = new ArrayList<>();
    while (results.next()) {
      columns.add(
          new FunctionColumn(
              results.getString("FUNCTION_CAT"),
              results.getString("FUNCTION_SCHEM"),
              results.getString("FUNCTION_NAME"),
              results.getString("COLUMN_NAME"),
              results.getShort("COLUMN_TYPE"),
              results.getInt("DATA_TYPE"),
              results.getString("TYPE_NAME"),
              results.getInt("PRECISION"),
              results.getInt("LENGTH"),
              results.getShort("SCALE"),
              results.getShort("RADIX"),
              results.getShort("NULLABLE"),
              results.getString("REMARKS"),
              results.getInt("CHAR_OCTET_LENGTH"),
              results.getInt("ORDINAL_POSITION"),
              results.getString("IS_NULLABLE"),
              results.getString("SPECIFIC_NAME")));
    }
    return columns;
  }

  public static List<StatementColumn> getStatementColumns(
      Connection connection,
      String sql) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(sql);

    ResultSetMetaData columnsMetadata = statement.getMetaData();
    List<StatementColumn> columns = new ArrayList<>();
    for (int i = 1; i < columnsMetadata.getColumnCount() + 1; i++) {
      columns.add(new StatementColumn(
          columnsMetadata.getCatalogName(i),
          columnsMetadata.getColumnClassName(i),
          columnsMetadata.getColumnDisplaySize(i),
          columnsMetadata.getColumnLabel(i),
          columnsMetadata.getColumnName(i),
          columnsMetadata.getColumnType(i),
          columnsMetadata.getColumnTypeName(i),
          columnsMetadata.getPrecision(i),
          columnsMetadata.getSchemaName(i),
          columnsMetadata.getTableName(i),
          columnsMetadata.isAutoIncrement(i),
          columnsMetadata.isCaseSensitive(i),
          columnsMetadata.isCurrency(i),
          columnsMetadata.isDefinitelyWritable(i),
          columnsMetadata.isNullable(i),
          columnsMetadata.isReadOnly(i),
          columnsMetadata.isSearchable(i),
          columnsMetadata.isSigned(i),
          columnsMetadata.isWritable(i)
      ));
    }

    return columns;
  }

  public static List<StatementParameter> getStatementParameters(
      Connection connection,
      String sql) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(sql);

    ParameterMetaData parametersMetadata = statement.getParameterMetaData();
    List<StatementParameter> parameters = new ArrayList<>();
    for (int i = 1; i < parametersMetadata.getParameterCount() + 1; i++) {
      parameters.add(new StatementParameter(
          parametersMetadata.getParameterClassName(i),
          parametersMetadata.getParameterMode(i),
          parametersMetadata.getParameterType(i),
          parametersMetadata.getParameterTypeName(i),
          parametersMetadata.getPrecision(i),
          parametersMetadata.getScale(i),
          parametersMetadata.isNullable(i),
          parametersMetadata.isSigned(i)
      ));
    }

    return parameters;
  }

}
