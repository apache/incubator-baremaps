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

package org.apache.baremaps.geoparquet;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.baremaps.geoparquet.data.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroReadSupport;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.FileMetaData;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.RecordReader;
import org.apache.parquet.schema.MessageType;
import org.locationtech.jts.io.WKBReader;

public class GeoParquetReader {

  private final URI uri;

  private Configuration configuration;

  private WKBReader wkbReader = new WKBReader();

  private Map<FileStatus, GeoParquetFileInfo> metadata = new LinkedHashMap<>();

  private long rowCount;

  public GeoParquetReader(URI uri) {
    this.uri = uri;
    this.initialize();
  }

  public void initialize() {
    this.rowCount = 0;
    this.configuration = getConfiguration();

    try {
      // List all the files that match the glob pattern
      Path globPath = new Path(uri.getPath());
      URI rootUri = getRootUri(uri);
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

          // Increment the total number of rows
          this.rowCount += rowCount;

          // Get the geometry columns of the Parquet file
          Set<String> geometryColumns = geoParquetMetadata.getColumns().keySet();

          // Store the metadata of the Parquet file
          this.metadata.put(fileStatus, new GeoParquetFileInfo(rowCount, parquetMetadata,
              geoParquetMetadata, geometryColumns));
        }
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Stream<GeoParquetGroup> read() throws IOException {
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(new GroupIterator(), Spliterator.ORDERED),
        false);
  }

  private static Configuration getConfiguration() {
    Configuration configuration = new Configuration();
    configuration.set("fs.s3a.aws.credentials.provider",
        "org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider");
    configuration.setBoolean("fs.s3a.path.style.access", true);
    configuration.setBoolean(AvroReadSupport.READ_INT96_AS_FIXED, true);
    return configuration;
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

  private class GroupIterator implements Iterator<GeoParquetGroup> {

    private Iterator<Map.Entry<FileStatus, GeoParquetFileInfo>> fileIterator;

    private Map.Entry<FileStatus, GeoParquetFileInfo> currentFileStatus;
    private Iterator<PageReadStore> pageReadStoreIterator;

    private PageReadStore currentPageReadStore;

    private Iterator<GeoParquetGroup> simpleGroupIterator;

    private GeoParquetGroup currentGeoParquetGroup;

    public GroupIterator() throws IOException {
      this.fileIterator = metadata.entrySet().iterator();
      this.currentFileStatus = fileIterator.next();
      this.pageReadStoreIterator = new PageReadStoreIterator(currentFileStatus);
      this.currentPageReadStore = pageReadStoreIterator.next();
      this.simpleGroupIterator = new FeatureGroupIterator(
          currentFileStatus.getValue(),
          currentPageReadStore);
      this.currentGeoParquetGroup = simpleGroupIterator.next();
    }

    @Override
    public boolean hasNext() {
      if (simpleGroupIterator.hasNext()) {
        return true;
      } else if (pageReadStoreIterator.hasNext()) {
        currentPageReadStore = pageReadStoreIterator.next();
        simpleGroupIterator = new FeatureGroupIterator(
            currentFileStatus.getValue(),
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
    public GeoParquetGroup next() {
      currentGeoParquetGroup = simpleGroupIterator.next();
      return currentGeoParquetGroup;
    }
  }

  private class PageReadStoreIterator implements Iterator<PageReadStore> {

    private final ParquetFileReader parquetFileReader;

    private final MessageType messageType;

    private PageReadStore next;

    public PageReadStoreIterator(Map.Entry<FileStatus, GeoParquetFileInfo> fileInfo)
        throws IOException {
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

  private static class FeatureGroupIterator implements Iterator<GeoParquetGroup> {
    private final long rowCount;
    private final RecordReader<GeoParquetGroup> recordReader;

    private long i = 0;

    private FeatureGroupIterator(GeoParquetFileInfo geoParquetFileInfo,
        PageReadStore pageReadStore) {
      this.rowCount = pageReadStore.getRowCount();

      MessageType schema = geoParquetFileInfo.getParquetMetadata().getFileMetaData().getSchema();
      this.recordReader = new ColumnIOFactory()
          .getColumnIO(schema)
          .getRecordReader(pageReadStore, new GeoParquetMaterializer(geoParquetFileInfo));
    }

    @Override
    public boolean hasNext() {
      return i <= rowCount;
    }

    @Override
    public GeoParquetGroup next() {
      i++;
      return recordReader.read();
    }
  }

}
