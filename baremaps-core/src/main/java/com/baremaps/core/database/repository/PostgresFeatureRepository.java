/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.core.database.repository;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Operator;
import com.esri.core.geometry.OperatorExportToWkb;
import com.esri.core.geometry.OperatorFactoryLocal;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.sis.feature.AbstractFeature;
import org.apache.sis.feature.AbstractIdentifiedType;
import org.apache.sis.feature.DefaultAttributeType;
import org.apache.sis.feature.DefaultFeatureType;

public class PostgresFeatureRepository {

  private final DefaultFeatureType featureType;

  private final DataSource dataSource;

  private String create;

  private String select;

  private String insert;

  private String drop;

  private String copy;

  public PostgresFeatureRepository(DataSource dataSource, DefaultFeatureType featureType) {
    this(dataSource, featureType, featureType.getName().toString().replace(".", "_"));
  }

  public PostgresFeatureRepository(DataSource dataSource, DefaultFeatureType featureType, String tableName) {
    this.dataSource = dataSource;
    this.featureType = featureType;
    String columnDefinitions =
        featureType.getProperties(false).stream()
            .map(this::columnDefinition)
            .collect(Collectors.joining(", "));
    String columnNames =
        featureType.getProperties(false).stream()
            .map(a -> a.getName().toString())
            .collect(Collectors.joining(", "));
    String columnValues =
        featureType.getProperties(false).stream().map(a -> "?").collect(Collectors.joining(", "));
    this.create = String.format("CREATE TABLE IF NOT EXISTS %s (%s)", tableName, columnDefinitions);
    this.insert =
        String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columnNames, columnValues);
    this.copy = String.format("COPY %s (%s) FROM STDIN BINARY", tableName, columnNames);
    this.drop = String.format("DROP TABLE IF EXISTS %s CASCADE", tableName);
    this.select = String.format("SELECT * FROM %s", tableName);
  }

  public void create() throws RepositoryException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(create)) {
      statement.execute();
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  public void drop() throws RepositoryException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(drop)) {
      statement.execute();
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  public void insert(AbstractFeature feature) throws RepositoryException {
    if (feature == null) {
      return;
    }
    if (!feature.getType().equals(featureType)) {
      throw new IllegalArgumentException(
          "The type of the feature is not compatible with this store.");
    }
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(insert)) {
      int i = 1;
      for (AbstractIdentifiedType type : featureType.getProperties(false)) {
        String name = type.getName().toString();
        Object value = feature.getPropertyValue(name);
        if (value instanceof Geometry) {
          OperatorExportToWkb operatorExport =
              (OperatorExportToWkb)
                  OperatorFactoryLocal.getInstance().getOperator(Operator.Type.ExportToWkb);
          ByteBuffer byteBuffer = operatorExport.execute(0, (Geometry) value, null);
          value = byteBuffer.array();
        }
        statement.setObject(i++, value);
      }
      statement.execute();
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  private String columnDefinition(AbstractIdentifiedType attributeType) {
    String name = attributeType.getName().toString();
    String type =
        columnType(((DefaultAttributeType) attributeType).getValueClass().getSimpleName());
    return String.format("%s %s", name, type);
  }

  private String columnType(String type) {
    switch (type) {
      case "Integer":
        return "integer";
      case "Number":
      case "Double":
      case "Float":
        return "numeric";
      case "Geometry":
        return "geometry";
      case "String":
      default:
        return "varchar";
    }
  }
}
