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
import java.util.*;
import java.util.stream.Collectors;
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
import org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider;
import org.apache.hadoop.fs.s3a.S3AFileSystem;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.schema.MessageType;


/**
 * This reader is based on the parquet example code located at: org.apache.parquet.example.data.*.
 */
public class GeoParquetReader {

  private final URI uri;
  final Configuration configuration;
  private List<FileStatus> files;
  private Long groupCount;

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

  public MessageType getParquetSchema() {
    return files().stream()
        .findFirst()
        .map(this::getFileInfo)
        .orElseThrow()
        .messageType();
  }

  private FileInfo getFileInfo(FileStatus fileStatus) {
    try {
      return buildFileInfo(fileStatus);
    } catch (IOException e) {
      throw new GeoParquetException("Failed to build Info", e);
    }
  }

  public GeoParquetMetadata getGeoParquetMetadata() {
    return files().stream()
        .findFirst()
        .map(this::getFileInfo)
        .orElseThrow()
        .metadata();
  }

  public Schema getGeoParquetSchema() {
    return files().stream()
        .findFirst()
        .map(this::getFileInfo)
        .orElseThrow()
        .geoParquetSchema();
  }

  public boolean validateSchemasAreIdentical() {
    // Verify that the files all have the same schema
    final int messageTypeCount = files().stream().parallel().map(this::getFileInfo)
        .map(FileInfo::messageType).collect(Collectors.toSet()).size();
    return messageTypeCount == 1;
  }

  public long size() {
    if (groupCount == null) {
      groupCount = files().stream().parallel().map(this::getFileInfo).map(FileInfo::recordCount)
          .reduce(0L, Long::sum);
    }
    return groupCount;
  }

  private synchronized List<FileStatus> files() {
    try {
      if (files == null) {
        Path globPath = new Path(uri.getPath());
        FileSystem fileSystem = FileSystem.get(uri, configuration);

        files = new ArrayList<>(Arrays.asList(fileSystem.globStatus(globPath)));
      }
    } catch (IOException e) {
      throw new GeoParquetException("IOException while attempting to list files.", e);
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

  public Stream<GeoParquetGroup> readParallel() {
    return retrieveGeoParquetGroups(true);
  }

  private Stream<GeoParquetGroup> retrieveGeoParquetGroups(boolean inParallel) {
    return StreamSupport.stream(new GeoParquetGroupSpliterator(this, files()), inParallel);
  }

  public Stream<GeoParquetGroup> read() {
    return retrieveGeoParquetGroups(false);
  }

  private static Configuration createConfiguration() {
    Configuration conf = new Configuration();
    conf.set("fs.s3a.endpoint", "s3.us-west-2.amazonaws.com");
    conf.set("fs.s3a.aws.credentials.provider", AnonymousAWSCredentialsProvider.class.getName());
    conf.set("fs.s3a.impl", S3AFileSystem.class.getName());
    conf.set("fs.s3a.path.style.access", "true");
    return conf;
  }
}
