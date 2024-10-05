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
import java.util.concurrent.atomic.AtomicLong;
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
import org.apache.parquet.hadoop.metadata.BlockMetaData;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.schema.MessageType;

/**
 * This reader is based on the parquet example code located at: org.apache.parquet.example.data.*.
 */
public class GeoParquetReader {

  private final URI uri;
  private final Configuration configuration;
  private final List<FileStatus> files;
  private final AtomicLong groupCount = new AtomicLong(-1);

  private static class FileInfo {
    final FileStatus file;
    final long recordCount;
    final Map<String, String> keyValueMetadata;
    final MessageType messageType;
    final GeoParquetMetadata metadata;
    final Schema geoParquetSchema;

    FileInfo(FileStatus file, long recordCount, Map<String, String> keyValueMetadata,
        MessageType messageType, GeoParquetMetadata metadata,
        Schema geoParquetSchema) {
      this.file = file;
      this.recordCount = recordCount;
      this.keyValueMetadata = keyValueMetadata;
      this.messageType = messageType;
      this.metadata = metadata;
      this.geoParquetSchema = geoParquetSchema;
    }
  }

  public GeoParquetReader(URI uri) {
    this(uri, createConfiguration());
  }

  public GeoParquetReader(URI uri, Configuration configuration) {
    this.uri = uri;
    this.configuration = configuration;
    this.files = initializeFiles();
  }

  private List<FileStatus> initializeFiles() {
    try {
      Path globPath = new Path(uri.getPath());
      FileSystem fileSystem = FileSystem.get(uri, configuration);
      FileStatus[] fileStatuses = fileSystem.globStatus(globPath);
      if (fileStatuses == null) {
        throw new GeoParquetException("No files found at the specified URI.");
      }
      return Collections.unmodifiableList(Arrays.asList(fileStatuses));
    } catch (IOException e) {
      throw new GeoParquetException("IOException while attempting to list files.", e);
    }
  }

  public MessageType getParquetSchema() {
    return files.stream()
        .findFirst()
        .map(this::getFileInfo)
        .orElseThrow(
            () -> new GeoParquetException("No files available to read schema.")).messageType;
  }

  private FileInfo getFileInfo(FileStatus fileStatus) {
    try {
      long recordCount;
      MessageType messageType;
      Map<String, String> keyValueMetadata;

      ParquetMetadata parquetMetadata =
          ParquetFileReader.readFooter(configuration, fileStatus.getPath());
      recordCount = parquetMetadata.getBlocks().stream()
          .mapToLong(BlockMetaData::getRowCount)
          .sum();

      messageType = parquetMetadata.getFileMetaData().getSchema();
      keyValueMetadata = parquetMetadata.getFileMetaData().getKeyValueMetaData();

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

      return new FileInfo(fileStatus, recordCount, keyValueMetadata, messageType,
          geoParquetMetadata, geoParquetSchema);
    } catch (IOException e) {
      throw new GeoParquetException("Failed to build FileInfo for file: " + fileStatus, e);
    }
  }

  public GeoParquetMetadata getGeoParquetMetadata() {
    return files.stream()
        .findFirst()
        .map(this::getFileInfo)
        .orElseThrow(
            () -> new GeoParquetException("No files available to read metadata.")).metadata;
  }

  public Schema getGeoParquetSchema() {
    return files.stream()
        .findFirst()
        .map(this::getFileInfo)
        .orElseThrow(
            () -> new GeoParquetException("No files available to read schema.")).geoParquetSchema;
  }

  public boolean validateSchemasAreIdentical() {
    // Verify that all files have the same schema
    Set<MessageType> schemas = files.parallelStream()
        .map(this::getFileInfo)
        .map(fileInfo -> fileInfo.messageType)
        .collect(Collectors.toSet());
    return schemas.size() == 1;
  }

  public long size() {
    if (groupCount.get() == -1) {
      long totalCount = files.parallelStream()
          .map(this::getFileInfo)
          .mapToLong(fileInfo -> fileInfo.recordCount)
          .sum();
      groupCount.set(totalCount);
    }
    return groupCount.get();
  }

  public Stream<GeoParquetGroup> readParallel() {
    return retrieveGeoParquetGroups(true);
  }

  private Stream<GeoParquetGroup> retrieveGeoParquetGroups(boolean inParallel) {
    Spliterator<GeoParquetGroup> spliterator =
        new GeoParquetGroupSpliterator(this, files, 0, files.size());
    return StreamSupport.stream(spliterator, inParallel);
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

  public URI getUri() {
    return uri;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public List<FileStatus> getFiles() {
    return files;
  }
}
