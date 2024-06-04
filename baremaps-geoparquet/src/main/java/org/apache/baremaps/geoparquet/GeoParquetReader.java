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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup.Schema;
import org.apache.baremaps.geoparquet.data.GeoParquetGroupFactory;
import org.apache.baremaps.geoparquet.data.GeoParquetMetadata;
import org.apache.baremaps.geoparquet.hadoop.GeoParquetGroupReadSupport;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider;
import org.apache.hadoop.fs.s3a.S3AFileSystem;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.schema.MessageType;


/**
 * This reader is based on the parquet example code located at: org.apache.parquet.example.data.*.
 */
public class GeoParquetReader {

  private final URI uri;

  private final Configuration configuration;

  private Map<FileStatus, FileInfo> files;

  private record FileInfo(FileStatus file, Long recordCount, Map<String, String> keyValueMetadata,
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
        FileSystem fs = FileSystem.get(uri, configuration);
        FileStatus[] fileStatuses = fs.globStatus(new Path(uri));

        for (FileStatus file : fileStatuses) {
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

  public Stream<GeoParquetGroup> readParallel() throws URISyntaxException {
    return StreamSupport.stream(
        new GeoParquetGroupSpliterator(files()),
        true);
  }

  public Stream<GeoParquetGroup> read() throws IOException, URISyntaxException {
    return readParallel().sequential();
  }

  public class GeoParquetGroupSpliterator implements Spliterator<GeoParquetGroup> {

    private final Queue<FileStatus> queue;
    private final Map<FileStatus, FileInfo> files;

    private FileStatus fileStatus;

    private ParquetReader<GeoParquetGroup> reader;

    GeoParquetGroupSpliterator(Map<FileStatus, FileInfo> files) {
      this.files = files;
      this.queue = new ArrayBlockingQueue<>(files.keySet().size(), false, files.keySet());

    }

    @Override
    public boolean tryAdvance(Consumer<? super GeoParquetGroup> action) {
      try {
        // Poll the next file
        if (fileStatus == null) {
          fileStatus = queue.poll();
        }

        // If there are no more files, return false
        if (fileStatus == null) {
          return false;
        }

        // Create a new reader if it does not exist
        if (reader == null) {
          reader = createParquetReader(fileStatus);
        }

        // Read the next group
        GeoParquetGroup group = reader.read();

        // If the group is null, close the resources and set the variables to null
        if (group == null) {
          reader.close();
          reader = null;
          fileStatus = null;

          // Try to advance again
          return tryAdvance(action);
        }

        // Accept the group and tell the caller that there are more groups to read
        action.accept(group);
        return true;

      } catch (IOException e) {
        // If an exception occurs, try to close the resources and throw a runtime exception
        if (reader != null) {
          try {
            reader.close();
          } catch (IOException e2) {
            // Ignore the exception as the original exception is more important
          }
        }
        throw new GeoParquetException("IOException caught while trying to read the next file.", e);
      }
    }

    private ParquetReader<GeoParquetGroup> createParquetReader(FileStatus file)
        throws IOException {
      return ParquetReader
          .builder(new GeoParquetGroupReadSupport(), file.getPath())
          .withConf(configuration)
          .build();
    }

    @Override
    public Spliterator<GeoParquetGroup> trySplit() {
      // Create a new spliterator by polling the next polledFileStatus
      FileStatus polledFileStatus = queue.poll();

      // If there are no more files, tell the caller that there is nothing to split anymore
      if (polledFileStatus == null) {
        return null;
      }

      // Return a new spliterator with the polledFileStatus
      return new GeoParquetGroupSpliterator(Map.of(polledFileStatus, files.get(polledFileStatus)));
    }

    @Override
    public long estimateSize() {
      // The size is unknown
      return files.values().stream()
          .map(FileInfo::recordCount)
          .reduce(0L, Long::sum);
    }

    @Override
    public int characteristics() {
      // The spliterator is not sized, ordered, or sorted
      return Spliterator.NONNULL | Spliterator.IMMUTABLE;
    }
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
