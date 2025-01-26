package org.apache.baremaps.calcite;

import org.apache.baremaps.data.collection.AppendOnlyLog;
import org.apache.baremaps.data.memory.MemoryMappedDirectory;
import org.apache.baremaps.data.store.DataTableImpl;
import org.apache.baremaps.data.type.DataTypeImpl;
import org.apache.baremaps.data.util.FileUtils;
import org.apache.baremaps.store.*;
import org.apache.baremaps.store.DataColumn.Cardinality;
import org.apache.baremaps.store.DataColumn.ColumnType;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.List;

class BaremapsTableFactoryTest {


    @Test
    public void createCsvTable() throws Exception {
        File file = File.createTempFile("test", ".csv");
        String csv = """
        ID,NAME,GEOM
        1,Paris,POINT(2.3522 48.8566)
        2,New York,POINT(-74.0060 40.7128)
        """;
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.write(csv);
        }
        String model = """
        {
            version: '1.0',
            defaultSchema: 'TEST',
            schemas: [
                {
                name: 'TEST',
                tables: [
                    {
                      name: 'TEST',
                      factory: 'org.apache.baremaps.calcite.BaremapsTableFactory',
                      operand: {
                          format: 'csv',
                          file: '%s'
                      }
                    }
                ]
              }
            ]
        }
        """.formatted(file.getAbsolutePath());
        try (Connection connection =
                     DriverManager.getConnection("jdbc:calcite:model=inline:" + model)) {

            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM TEST.TEST");
            while (resultSet.next()) {
                System.out.println(resultSet.getString("ID") + " " + resultSet.getString("GEOM"));
            }
        } finally {
            file.delete();
        }
    }

    @Test
    public void createMMapTable() throws Exception {
        Path path = Files.createTempDirectory("temp");

        DataSchema dataSchema = new DataSchemaImpl("test", List.of(
                new DataColumnFixed("id", Cardinality.REQUIRED, ColumnType.INTEGER),
                new DataColumnFixed("name", Cardinality.REQUIRED, ColumnType.STRING),
                new DataColumnFixed("geom", Cardinality.REQUIRED, ColumnType.GEOMETRY)
        ));

        DataTable dataTable = new DataTableImpl(dataSchema, new AppendOnlyLog<>(new DataTypeImpl(dataSchema), new MemoryMappedDirectory(path)));
        dataTable.add(new DataRowImpl(dataSchema, List.of(1, "a", new GeometryFactory().createPoint(new Coordinate(1, 1)))));
        dataTable.add(new DataRowImpl(dataSchema, List.of(2, "b", new GeometryFactory().createPoint(new Coordinate(2, 2)))));
        dataTable.close();

        String model = """
        {
            version: '1.0',
            defaultSchema: 'TEST',
            schemas: [
                {
                name: 'TEST',
                tables: [
                    {
                      name: 'TEST',
                      factory: 'org.apache.baremaps.calcite.BaremapsTableFactory',
                      operand: {
                          format: 'mmap',
                          directory: '%s'
                      }
                    }
                ]
              }
            ]
        }
        """.formatted(path.toAbsolutePath());
        try (Connection connection =
                     DriverManager.getConnection("jdbc:calcite:model=inline:" + model)) {

            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM TEST.TEST");
            while (resultSet.next()) {
                System.out.println(resultSet.getString("ID") + " " + resultSet.getString("GEOM"));
            }
        } finally {
            FileUtils.deleteRecursively(path);
        }
    }
}