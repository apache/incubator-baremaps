package io.gazetteer.boilerplate.postgis;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;

public class PostgisBoilerplate {

  public void generate(String url, String catalog, String schemaPattern, String tablePattern, String packageName, Path outputDirectory)
      throws SQLException, IOException, ClassNotFoundException {

    // Delete the output directory if any
    if (Files.exists(outputDirectory)) {
      Files.walk(outputDirectory)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    }

    // Initialize the connection
    try (Connection connection = DriverManager.getConnection(url)) {

      //((PGConnection)connection).addDataType("hstore", Geometry.class);


      // Iterate over the tables corresponding to the table pattern
      for (Table tableMetadata : MetadataUtil.getTables(connection, catalog, schemaPattern, tablePattern)) {
        String tableName = tableMetadata.getTableName();

        // Get the metadata associated with the columns
        List<TableColumn> columnsMetadata = MetadataUtil
            .getTableColumns(connection, catalog, schemaPattern, tableName, null);
        List<String> columnNames = columnsMetadata
            .stream().map(c -> c.getColumnName()).collect(Collectors.toList());

        // Infer the column types from a prepared statement
        List<StatementColumn> columns = MetadataUtil
            .getStatementColumns(connection, QueryUtil.select(tableName, columnNames));

        // Create class that correspond to the table
        TypeSpec.Builder classBuilder = TypeSpec
            .classBuilder(Conversions.className(tableMetadata.getTableName()))
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        // Add internal class to wrap rows
        addInternalClass(classBuilder, "Row", columns);

        // Add constant for insert query
        addConstant(classBuilder, "INSERT", QueryUtil.insert(tableName, columnNames));

        // Add method that init insert statement
        addCreateStatementMethod(classBuilder, "createInsert", "INSERT");

        // Add method that execute an insert statement
        Builder insertBuilder = MethodSpec.methodBuilder("insert")
            .addException(SQLException.class)
            .returns(int.class)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(Connection.class, "connection")
            .addParameter(ParameterSpec.builder(TypeVariableName.get("Row"), "row").build());
        insertBuilder.beginControlFlow("try ($T statement = connection.prepareStatement(INSERT))", PreparedStatement.class);
        addColumnSetters(insertBuilder, columns, "row.", 1);
        insertBuilder.addStatement("return statement.executeUpdate()");
        insertBuilder.endControlFlow();
        classBuilder.addMethod(insertBuilder.build());

        // Add method that batch an insert
        Builder batchInsertBuilder = MethodSpec.methodBuilder("batchInsert")
            .addException(SQLException.class)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(PreparedStatement.class, "statement")
            .addParameter(ParameterSpec.builder(TypeVariableName.get("Row"), "row").build());
        batchInsertBuilder.addStatement("statement.clearParameters()");
        addColumnSetters(batchInsertBuilder, columns, "row.", 1);
        batchInsertBuilder.addStatement("statement.addBatch()");
        classBuilder.addMethod(batchInsertBuilder.build());

        // Get the metadata associated with the primary key
        List<PrimaryKeyColumn> primaryKeyMetadata = MetadataUtil
            .getPrimaryKeyColumns(connection, catalog, schemaPattern, tableName);
        List<String> primaryKeyColumnNames = primaryKeyMetadata
            .stream().map(c -> c.getColumnName()).collect(Collectors.toList());

        // Infer the primary key types from a prepared statement
        List<StatementColumn> primaryKey = MetadataUtil
            .getStatementColumns(connection, QueryUtil.select(tableName, primaryKeyColumnNames));

        // A record must be identifiable in order to be selected, updated or deleted
        if (primaryKey.size() > 0) {

          // Add internal class to wrap rows
          addInternalClass(classBuilder, "PrimaryKey", primaryKey);

          // Add constant for select query
          addConstant(classBuilder, "SELECT", QueryUtil.select(tableName, columnNames, QueryUtil.where(primaryKeyColumnNames)));

          // Add method that init select statement
          addCreateStatementMethod(classBuilder, "createSelect", "SELECT");

          // Add method that perform the execute a select
          Builder selectBuilder = MethodSpec.methodBuilder("select")
              .returns(TypeVariableName.get("Row"))
              .addException(SQLException.class)
              .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
              .addParameter(Connection.class, "connection")
              .addParameter(ParameterSpec.builder(TypeVariableName.get("PrimaryKey"), "primaryKey").build());
          selectBuilder.beginControlFlow("try($T statement = connection.prepareStatement(SELECT))", PreparedStatement.class);
          addColumnSetters(selectBuilder, primaryKey, "primaryKey.", 1);
          selectBuilder.addStatement("$T result = statement.executeQuery()", ResultSet.class);
          selectBuilder.beginControlFlow("if (!result.next())");
          selectBuilder.addStatement("return null");
          selectBuilder.nextControlFlow("else");
          selectBuilder.addCode("return new Row(\n");
          int selectResultIdx = 1;
          for (StatementColumn column : columns) {
            String suffix = selectResultIdx < columns.size() ? ",\n" : "\n";
            selectBuilder
                .addCode("  ($T) result.getObject($L)$L", Class.forName(column.getColumnClassName()), selectResultIdx++, suffix);
          }
          selectBuilder.addCode(");\n");
          selectBuilder.endControlFlow();
          selectBuilder.endControlFlow();
          classBuilder.addMethod(selectBuilder.build());

          // Add constant for update query
          addConstant(classBuilder, "UPDATE", QueryUtil.update(tableName, columnNames, QueryUtil.where(primaryKeyColumnNames)));

          // Add method that create update statement
          addCreateStatementMethod(classBuilder, "createUpdate", "UPDATE");

          // Add method that execute an update
          Builder updateBuilder = MethodSpec.methodBuilder("update")
              .addException(SQLException.class)
              .returns(int.class)
              .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
              .addParameter(Connection.class, "connection")
              .addParameter(ParameterSpec.builder(TypeVariableName.get("Row"), "row").build())
              .addParameter(ParameterSpec.builder(TypeVariableName.get("PrimaryKey"), "primaryKey").build());
          updateBuilder.beginControlFlow("try ($T statement = connection.prepareStatement(UPDATE))", PreparedStatement.class);
          addColumnSetters(updateBuilder, columns, "row.", 1);
          addColumnSetters(updateBuilder, primaryKey, "primaryKey.", 1 + columns.size());
          updateBuilder.addStatement("return statement.executeUpdate()");
          updateBuilder.endControlFlow();
          classBuilder.addMethod(updateBuilder.build());

          // Add method that batch an update
          Builder batchUpdateBuilder = MethodSpec.methodBuilder("batchUpdate")
              .addException(SQLException.class)
              .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
              .addParameter(PreparedStatement.class, "statement")
              .addParameter(ParameterSpec.builder(TypeVariableName.get("Row"), "row").build())
              .addParameter(ParameterSpec.builder(TypeVariableName.get("PrimaryKey"), "primaryKey").build())
              .addStatement("statement.clearParameters()");
          addColumnSetters(batchUpdateBuilder, columns, "row.", 1);
          addColumnSetters(batchUpdateBuilder, primaryKey, "primaryKey.", 1 + columns.size());
          batchUpdateBuilder.addStatement("statement.addBatch()");
          classBuilder.addMethod(batchUpdateBuilder.build());

          // Add constant for delete query
          addConstant(classBuilder, "DELETE", QueryUtil.delete(tableName, QueryUtil.where(primaryKeyColumnNames)));

          // Add method that create delete statement
          addCreateStatementMethod(classBuilder, "createDelete", "DELETE");

          // Add method that execute a delete
          Builder deleteBuilder = MethodSpec.methodBuilder("delete")
              .addException(SQLException.class)
              .returns(int.class)
              .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
              .addParameter(Connection.class, "connection")
              .addParameter(ParameterSpec.builder(TypeVariableName.get("PrimaryKey"), "primaryKey").build());
          deleteBuilder.beginControlFlow("try ($T statement = connection.prepareStatement(DELETE))", PreparedStatement.class);
          addColumnSetters(deleteBuilder, primaryKey, "primaryKey.", 1);
          deleteBuilder.addStatement("return statement.executeUpdate()");
          deleteBuilder.endControlFlow();
          classBuilder.addMethod(deleteBuilder.build());

          // Add method that batch a delete
          Builder batchDeleteBuilder = MethodSpec.methodBuilder("batchDelete")
              .addException(SQLException.class)
              .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
              .addParameter(PreparedStatement.class, "statement")
              .addParameter(ParameterSpec.builder(TypeVariableName.get("PrimaryKey"), "primaryKey").build());
          batchDeleteBuilder.addStatement("statement.clearParameters()");
          addColumnSetters(batchDeleteBuilder, primaryKey, "primaryKey.", 1);
          batchDeleteBuilder.addStatement("statement.addBatch()");
          classBuilder.addMethod(batchDeleteBuilder.build());
        }

        // Check wether the database is Postgresql before leveraging the CopyManager
        if (connection instanceof PGConnection) {
          PGConnection pg = (PGConnection) connection;
          CopyManager copyManager = pg.getCopyAPI();

          // Add constant for delete query
          addConstant(classBuilder, "COPY_IN", QueryUtil.copyIn(tableName, columnNames));

          // Add copyIn method
          Builder copyInBuilder = MethodSpec.methodBuilder("copyIn")
              .returns(CopyIn.class)
              .addException(SQLException.class)
              .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
              .addParameter(PGConnection.class, "connection");
          copyInBuilder.addStatement("$T copyManager = connection.getCopyAPI()", CopyManager.class);
          copyInBuilder.addStatement("return copyManager.copyIn(COPY_IN)");
          classBuilder.addMethod(copyInBuilder.build());
        }

        // Build the class and write it to the output directory
        TypeSpec entity = classBuilder.build();
        JavaFile javaFile = JavaFile.builder(packageName, entity).build();
        outputDirectory.toFile().mkdirs();
        javaFile.writeTo(outputDirectory);
      }
    }
  }

  private static void addInternalClass(TypeSpec.Builder tableClassBuilder, String className, List<StatementColumn> columns)
      throws ClassNotFoundException {
    // Create internal class
    TypeSpec.Builder rowClassBuilder = TypeSpec
        .classBuilder(className)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);

    // Create constructor
    Builder rowConstructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
    for (StatementColumn column : columns) {
      String columnName = column.getColumnName();
      String variableName = Conversions.variableName(columnName);
      Class<?> type = Class.forName(column.getColumnClassName());
      rowClassBuilder.addField(type, variableName, Modifier.PUBLIC, Modifier.FINAL);
      rowConstructorBuilder
          .addParameter(type, variableName)
          .addStatement("this.$1L = $1L", variableName);
    }
    rowClassBuilder.addMethod(rowConstructorBuilder.build());

    // Add the class to the table class
    tableClassBuilder.addType(rowClassBuilder.build());
  }

  private static void addConstant(TypeSpec.Builder classBuilder, String constantName, String constantValue) {
    classBuilder.addField(FieldSpec.builder(String.class, constantName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .initializer("$S", constantValue).build());
  }

  private static void addCreateStatementMethod(TypeSpec.Builder classBuilder, String methodName, String constantName) {
    classBuilder.addMethod(MethodSpec
        .methodBuilder(methodName)
        .addException(SQLException.class)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addParameter(Connection.class, "connection")
        .returns(PreparedStatement.class)
        .addStatement("return connection.prepareStatement($L)", constantName)
        .build());
  }

  private static void addColumnParameters(MethodSpec.Builder methodBuilder, List<StatementColumn> columns) throws ClassNotFoundException {
    for (StatementColumn column : columns) {
      String variableName = Conversions.variableName(column.getColumnName());
      Class variableType = Class.forName(column.getColumnClassName());
      methodBuilder.addParameter(variableType, variableName);
    }
  }

  private static void addColumnSetters(MethodSpec.Builder methodBuilder, List<StatementColumn> columns, String variablePrefix,
      int start) {
    for (StatementColumn column : columns) {
      String variableName = Conversions.variableName(column.getColumnName());
      Integer columnType = column.getColumnType();
      methodBuilder.addStatement("statement.setObject($1L, $2L$3L, $4L)", start++, variablePrefix, variableName, columnType);
    }
  }


}


