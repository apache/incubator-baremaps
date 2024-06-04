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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup.Schema;
import org.apache.baremaps.geoparquet.data.GeoParquetGroupFactory;
import org.apache.baremaps.geoparquet.data.GeoParquetMetadata;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.schema.MessageType;


/**
 * This reader is based on the parquet example code located at: org.apache.parquet.example.data.*.
 */
public class GeoParquetReader {

  private final URI uri;

  final Configuration configuration;

  private Map<FileStatus, FileInfo> files;

  record FileInfo(FileStatus file, Long recordCount, Map<String, String> keyValueMetadata,
      MessageType messageType, GeoParquetMetadata metadata,
      GeoParquetGroup.Schema geoParquetSchema) {
  }

  public GeoParquetReader(URI uri) {
    this(uri, createConfiguration());
  }

  public GeoParquetReader(URI uri, Configuration configuration) {
    this.uri = uri;
    this.configuration = configuration;
  }

  public MessageType getParquetSchema() throws URISyntaxException {
    return files().values().stream()
        .findFirst()
        .orElseThrow()
        .messageType();
  }

  public GeoParquetMetadata getGeoParquetMetadata() throws URISyntaxException {
    return files().values().stream()
        .findFirst()
        .orElseThrow()
        .metadata();
  }

  public Schema getGeoParquetSchema() throws URISyntaxException {
    return files().values().stream()
        .findFirst()
        .orElseThrow()
        .geoParquetSchema();
  }

  public Long size() throws URISyntaxException {
    return files().values().stream().map(FileInfo::recordCount).reduce(0L, Long::sum);
  }

  private synchronized Map<FileStatus, FileInfo> files() throws URISyntaxException {
    try {
      if (files == null) {
        files = new HashMap<>();
        Path globPath = new Path(uri.getPath());
        URI rootUri = getRootUri(uri);
        FileSystem fileSystem = FileSystem.get(rootUri, configuration);

        // Iterate over all the files in the path
        for (FileStatus file : fileSystem.globStatus(globPath)) {
          files.put(file, buildFileInfo(file));
        }

        // Verify that the files all have the same schema
        MessageType commonMessageType = null;
        for (FileInfo entry : files.values()) {
          if (commonMessageType == null) {
            commonMessageType = entry.messageType;
          } else if (!commonMessageType.equals(entry.messageType)) {
            throw new GeoParquetException("The files do not have the same schema");
          }
        }
      }
    } catch (IOException e) {
      throw new GeoParquetException("IOException while  attempting to list files.", e);
    }
    return files;
  }

  private FileInfo buildFileInfo(FileStatus file) throws IOException {
    long recordCount;
    MessageType messageType;
    Map<String, String> keyValueMetadata;
    try (ParquetFileReader reader = ParquetFileReader.open(configuration, file.getPath())) {
      recordCount = reader.getRecordCount();
      messageType = reader.getFileMetaData().getSchema();
      keyValueMetadata = reader.getFileMetaData().getKeyValueMetaData();
    }
    GeoParquetMetadata geoParquetMetadata = null;
    Schema geoParquetSchema = null;
    if (keyValueMetadata.containsKey("geo")) {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      geoParquetMetadata =
          objectMapper.readValue(keyValueMetadata.get("geo"), GeoParquetMetadata.class);
      geoParquetSchema =
          GeoParquetGroupFactory.createGeoParquetSchema(messageType, geoParquetMetadata);
    }
    return new FileInfo(file, recordCount, keyValueMetadata, messageType,
        geoParquetMetadata, geoParquetSchema);
  }

  public Stream<GeoParquetGroup> readParallel() throws URISyntaxException {
    return StreamSupport.stream(new GeoParquetGroupSpliterator(this, files()), true);
  }

  public Stream<GeoParquetGroup> read() throws IOException, URISyntaxException {
    return readParallel().sequential();
  }

  private static Configuration createConfiguration() {
    Configuration configuration = new Configuration();
    configuration.set("fs.s3a.aws.credentials.provider",
        "org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider");
    configuration.setBoolean("fs.s3a.path.style.access", true);
    return configuration;
  }

  private static URI getRootUri(URI uri) throws URISyntaxException {
    // TODO:
    // This is a quick and dirty way to get the root uri of the path.
    // We take everything before the first wildcard in the path.
    // This is not a perfect solution, and we should probably look for a better way to do this.
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

}
