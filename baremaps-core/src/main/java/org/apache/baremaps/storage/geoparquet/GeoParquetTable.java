/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.storage.geoparquet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.StreamSupport;
import org.apache.baremaps.database.collection.AbstractDataCollection;
import org.apache.baremaps.database.schema.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroReadSupport;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.SimpleGroup;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.FileMetaData;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.RecordReader;
import org.apache.parquet.schema.LogicalTypeAnnotation;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Type;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBReader;

public class GeoParquetTable extends AbstractDataCollection<DataRow> implements DataTable {

  private URI uri;

  private Configuration configuration;

  private WKBReader wkbReader = new WKBReader();

  private Map<FileStatus, FileInfo> metadata = new HashMap<>();

  private DataRowType rowType;

  private Set<String> geometryColumns;

  private long rowCount;

  record FileInfo(
      long rowCount,
      ParquetMetadata parquetMetadata,
      GeoParquetMetadata geoParquetMetadata,
      DataRowType dataRowType) {
  }

  public GeoParquetTable(String uri) {
      this.uri = URI.create(uri);
        this.configuration = getConfiguration();

        try {
            // List all the files that match the glob pattern
            Path globPath = new Path(this.uri.getPath());
            URI rootUri = getRootUri(this.uri);
            FileSystem fileSystem = FileSystem.get(rootUri, configuration);
            List<FileStatus> files = Arrays.asList(fileSystem.globStatus(globPath));

            // Read the metadata of each file
            for (FileStatus fileStatus : files) {

                // Open the Parquet file
                try (ParquetFileReader reader = ParquetFileReader
                        .open(HadoopInputFile.fromPath(fileStatus.getPath(), configuration))) {

                    // Read the number of rows in the Parquet file
                    long rowCount = reader.getRecordCount();

                    // Read the metadata of the Parquet file
                    ParquetMetadata parquetMetadata = reader.getFooter();
                    FileMetaData fileMetadata = parquetMetadata.getFileMetaData();

                    // Read the GeoParquet metadata of the Parquet file
                    String json = fileMetadata.getKeyValueMetaData().get("geo");
                    GeoParquetMetadata geoParquetMetadata = new ObjectMapper()
                            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                            .readValue(json, GeoParquetMetadata.class);

                    // Read the schema of the Parquet file and create the corresponding row type
                    List<DataColumn> dataColumns = new ArrayList<>();
                    List<Type> types = fileMetadata.getSchema().getFields();
                    for (Type type : types) {
                        String name = type.getName();

                        // Handle the primitive types
                        if (type.isPrimitive()) {
                            PrimitiveType primitiveType = type.asPrimitiveType();

                            // Map the Parquet primitive types to the data column types
                            DataColumn.Type columnType = switch (primitiveType.getPrimitiveTypeName()) {
                                case BINARY -> {
                                    if (primitiveType.getLogicalTypeAnnotation() == LogicalTypeAnnotation.stringType()) {
                                        yield DataColumn.Type.STRING;
                                    } else {
                                        yield DataColumn.Type.BYTE_ARRAY;
                                    }
                                }
                                case INT64 -> DataColumn.Type.LONG;
                                case INT32 -> DataColumn.Type.INTEGER;
                                case BOOLEAN -> DataColumn.Type.BOOLEAN;
                                case FLOAT -> DataColumn.Type.FLOAT;
                                case DOUBLE -> DataColumn.Type.DOUBLE;
                                case INT96 -> DataColumn.Type.BYTE_ARRAY;
                                case FIXED_LEN_BYTE_ARRAY -> DataColumn.Type.BYTE_ARRAY;
                            };

                            // Map the GeoParquet geometry columns to the data column types
                            if (geoParquetMetadata.getColumns().containsKey(name)) {
                                columnType = DataColumn.Type.GEOMETRY;
                            }

                            dataColumns.add(new DataColumnImpl(name, columnType));
                        }
                    }
                    DataRowType dataRowType = new DataRowTypeImpl(uri.toString(), dataColumns);

                    // Store the metadata of the Parquet file
                    this.metadata.put(fileStatus, new FileInfo(rowCount, parquetMetadata, geoParquetMetadata, dataRowType));
                }
            }

            // Compute the total number of rows,
            // check the consistency of the row types,
            // and store the geometry columns
            for (FileInfo fileInfo : metadata.values()) {
                rowCount += fileInfo.rowCount();
                if (rowType == null) {
                    rowType = fileInfo.dataRowType();
                    geometryColumns = fileInfo.geoParquetMetadata().getColumns().keySet();
                } else if (!rowType.equals(fileInfo.dataRowType())) {
                    throw new IllegalArgumentException("Inconsistent row types");
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

  private static Configuration getConfiguration() {
    Configuration configuration = new Configuration();
    configuration.set("fs.s3a.aws.credentials.provider",
        "org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider");
    configuration.setBoolean("fs.s3a.path.style.access", true);
    configuration.setBoolean(AvroReadSupport.READ_INT96_AS_FIXED, true);
    return configuration;
  }

  @Override
  public Iterator<DataRow> iterator() {
    try {
      return StreamSupport
          .stream(Spliterators.spliteratorUnknownSize(new DataRowIterator(), 0),
              false)
          .map(values -> (DataRow) new DataRowImpl(rowType(), values))
          .iterator();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long sizeAsLong() {
    return rowCount;
  }

  @Override
  public DataRowType rowType() {
    return rowType;
  }

  private List<Object> asValues(GeoParquetMetadata geoParquetMetadata, SimpleGroup simpleGroup) {
        List<Object> values = new ArrayList<>();
        List<Type> fields = simpleGroup.getType().getFields();
        for (int i = 0; i < fields.size(); i++) {
            Type field = fields.get(i);
            String name = field.getName();
            if (field.isPrimitive()) {
                PrimitiveType primitiveType = field.asPrimitiveType();
                Object value;
                try {
                    value = switch (primitiveType.getPrimitiveTypeName()) {
                        case BINARY -> {
                            if (primitiveType.getLogicalTypeAnnotation() == LogicalTypeAnnotation.stringType()) {
                                yield simpleGroup.getString(i, 0);
                            } else {
                                yield simpleGroup.getBinary(i, 0).getBytes();
                            }
                        }
                        case INT64 -> simpleGroup.getLong(i, 0);
                        case INT32 -> simpleGroup.getInteger(i, 0);
                        case BOOLEAN -> simpleGroup.getBoolean(i, 0);
                        case FLOAT -> simpleGroup.getFloat(i, 0);
                        case DOUBLE -> simpleGroup.getDouble(i, 0);
                        case INT96 -> simpleGroup.getInt96(i, 0).getBytes();
                        case FIXED_LEN_BYTE_ARRAY -> simpleGroup.getBinary(i, 0).getBytes();
                    };
                    if (geometryColumns.contains(name) && value instanceof byte[] bytes) {
                        Geometry geometry = wkbReader.read(bytes);
                        int srid = getSrid(geoParquetMetadata, name);
                        geometry.setSRID(srid);
                        value = geometry;
                    }
                } catch (Exception e) {
                    value = null;
                }
                values.add(value);
            }
        }
        return values;
    }

  private static int getSrid(GeoParquetMetadata geoParquetMetadata, String name) {
    JsonNode crsId = geoParquetMetadata.getColumns().get(name).getCrs().get("id");
    int srid = switch (crsId.get("authority").asText()) {
      case "OGC" -> switch (crsId.get("code").asText()) {
          case "CRS84" -> 4326;
          default -> 0;
        };
      case "EPSG" -> crsId.get("code").asInt();
      default -> 0;
    };
    return srid;
  }

  private static URI getRootUri(URI uri) throws URISyntaxException {
    String path = uri.getPath();
    int index = path.indexOf("*");
    if (index != -1) {
      path = path.substring(0, path.lastIndexOf("/", index) + 1);
    }
    return new URI(
        uri.getScheme(),
        uri.getUserInfo(),
        uri.getHost(),
        uri.getPort(),
        path,
        null,
        null);
  }

  private class DataRowIterator implements Iterator<List<Object>> {

    private Iterator<Map.Entry<FileStatus, FileInfo>> fileIterator;

    private Map.Entry<FileStatus, FileInfo> currentFileStatus;
    private Iterator<PageReadStore> pageReadStoreIterator;

    private PageReadStore currentPageReadStore;

    private Iterator<SimpleGroup> simpleGroupIterator;

    private SimpleGroup currentSimpleGroup;

    public DataRowIterator() throws IOException {
      this.fileIterator = metadata.entrySet().iterator();
      this.currentFileStatus = fileIterator.next();
      this.pageReadStoreIterator = new PageReadStoreIterator(currentFileStatus);
      this.currentPageReadStore = pageReadStoreIterator.next();
      this.simpleGroupIterator = new SimpleGroupIterator(
          currentFileStatus.getValue().parquetMetadata().getFileMetaData().getSchema(),
          currentPageReadStore);
      this.currentSimpleGroup = simpleGroupIterator.next();
    }

    @Override
    public boolean hasNext() {
      if (simpleGroupIterator.hasNext()) {
        return true;
      } else if (pageReadStoreIterator.hasNext()) {
        currentPageReadStore = pageReadStoreIterator.next();
        simpleGroupIterator = new SimpleGroupIterator(
            currentFileStatus.getValue().parquetMetadata().getFileMetaData().getSchema(),
            currentPageReadStore);
        return hasNext();
      } else if (fileIterator.hasNext()) {
        currentFileStatus = fileIterator.next();
        try {
          pageReadStoreIterator = new PageReadStoreIterator(currentFileStatus);
          return hasNext();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else {
        return false;
      }
    }

    @Override
    public List<Object> next() {
      currentSimpleGroup = simpleGroupIterator.next();
      return asValues(currentFileStatus.getValue().geoParquetMetadata(), currentSimpleGroup);
    }
  }

  private class PageReadStoreIterator implements Iterator<PageReadStore> {

    private final ParquetFileReader parquetFileReader;

    private final MessageType messageType;

    private PageReadStore next;

    public PageReadStoreIterator(Map.Entry<FileStatus, FileInfo> fileInfo) throws IOException {
      this.parquetFileReader = ParquetFileReader
          .open(HadoopInputFile.fromPath(fileInfo.getKey().getPath(), configuration));
      this.messageType = this.parquetFileReader.getFooter().getFileMetaData().getSchema();
      try {
        next = parquetFileReader.readNextRowGroup();
      } catch (IOException e) {
        parquetFileReader.close();
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean hasNext() {
      boolean hasNext = next != null;
      if (!hasNext) {
        try {
          parquetFileReader.close();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      return hasNext;
    }

    @Override
    public PageReadStore next() {
      try {
        PageReadStore current = next;
        next = parquetFileReader.readNextRowGroup();
        if (next == null) {
          try {
            parquetFileReader.close();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
        return current;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private class SimpleGroupIterator implements Iterator<SimpleGroup> {

    private final long rowCount;
    private final RecordReader<Group> recordReader;

    private long i = 0;

    private SimpleGroupIterator(MessageType schema, PageReadStore pageReadStore) {
      this.rowCount = pageReadStore.getRowCount();
      this.recordReader = new ColumnIOFactory()
          .getColumnIO(schema)
          .getRecordReader(pageReadStore, new GroupRecordConverter(schema));
    }

    @Override
    public boolean hasNext() {
      return i <= rowCount;
    }

    @Override
    public SimpleGroup next() {
      i++;
      return (SimpleGroup) recordReader.read();
    }
  }

  private static class GeoParquetMetadata {

    @JsonProperty("version")
    private String version;

    @JsonProperty("primary_column")
    private String primaryColumn;

    @JsonProperty("columns")
    private Map<String, GeoParquetColumnMetadata> columns;

    public GeoParquetMetadata() {}

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
    }

    public String getPrimaryColumn() {
      return primaryColumn;
    }

    public void setPrimaryColumn(String primaryColumn) {
      this.primaryColumn = primaryColumn;
    }

    public Map<String, GeoParquetColumnMetadata> getColumns() {
      return columns;
    }

    public void setColumns(Map<String, GeoParquetColumnMetadata> columns) {
      this.columns = columns;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      GeoParquetMetadata that = (GeoParquetMetadata) o;
      return Objects.equal(version, that.version)
          && Objects.equal(primaryColumn, that.primaryColumn)
          && Objects.equal(columns, that.columns);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(version, primaryColumn, columns);
    }
  }

  private static class GeoParquetColumnMetadata {

    @JsonProperty("encoding")
    private String encoding;

    @JsonProperty("geometry_types")
    private List<String> geometryTypes;

    @JsonProperty("crs")
    private JsonNode crs;

    @JsonProperty("orientation")
    private String orientation;

    @JsonProperty("edges")
    private String edges;

    @JsonProperty("bbox")
    private Double[] bbox;

    public GeoParquetColumnMetadata() {}

    public String getEncoding() {
      return encoding;
    }

    public void setEncoding(String encoding) {
      this.encoding = encoding;
    }

    public List<String> getGeometryTypes() {
      return geometryTypes;
    }

    public void setGeometryTypes(List<String> geometryTypes) {
      this.geometryTypes = geometryTypes;
    }

    public JsonNode getCrs() {
      return crs;
    }

    public void setCrs(JsonNode crs) {
      this.crs = crs;
    }

    public String getOrientation() {
      return orientation;
    }

    public void setOrientation(String orientation) {
      this.orientation = orientation;
    }

    public String getEdges() {
      return edges;
    }

    public void setEdges(String edges) {
      this.edges = edges;
    }

    public Double[] getBbox() {
      return bbox;
    }

    public void setBbox(Double[] bbox) {
      this.bbox = bbox;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      GeoParquetColumnMetadata that = (GeoParquetColumnMetadata) o;
      return Objects.equal(encoding, that.encoding)
          && Objects.equal(geometryTypes, that.geometryTypes)
          && Objects.equal(crs, that.crs)
          && Objects.equal(orientation, that.orientation)
          && Objects.equal(edges, that.edges)
          && Objects.equal(bbox, that.bbox);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(encoding, geometryTypes, crs, orientation, edges, bbox);
    }
  }

}
