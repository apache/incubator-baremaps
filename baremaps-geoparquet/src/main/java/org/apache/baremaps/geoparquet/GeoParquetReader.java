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

  public MessageType getParquetSchema() throws IOException, URISyntaxException {
    return files().values().stream()
        .findFirst()
        .orElseThrow()
        .messageType();
  }

  public GeoParquetMetadata getGeoParquetMetadata() throws IOException, URISyntaxException {
    return files().values().stream()
        .findFirst()
        .orElseThrow()
        .metadata();
  }

  public Schema getGeoParquetSchema() throws IOException, URISyntaxException {
    return files().values().stream()
        .findFirst()
        .orElseThrow()
        .geoParquetSchema();
  }

  public Long size() throws URISyntaxException {
    return files().values().stream().map(fileInfo -> fileInfo.recordCount()).reduce(0L, Long::sum);
  }

  private Map<FileStatus, FileInfo> files() throws URISyntaxException {
    try {
      if (files == null) {
        files = new HashMap<>();
        Path globPath = new Path(uri.getPath());
        URI rootUri = getRootUri(uri);
        FileSystem fileSystem = FileSystem.get(rootUri, configuration);

        // Iterate over all the files in the path
        for (FileStatus file : fileSystem.globStatus(globPath)) {
          ParquetFileReader reader = ParquetFileReader.open(configuration, file.getPath());
          Long recordCount = reader.getRecordCount();
          MessageType messageType = reader.getFileMetaData().getSchema();
          Map<String, String> keyValueMetadata = reader.getFileMetaData().getKeyValueMetaData();
          GeoParquetMetadata geoParquetMetadata = null;
          GeoParquetGroup.Schema geoParquetSchema = null;
          if (keyValueMetadata.containsKey("geo")) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            geoParquetMetadata =
                objectMapper.readValue(keyValueMetadata.get("geo"), GeoParquetMetadata.class);
            geoParquetSchema =
                GeoParquetGroupFactory.createGeoParquetSchema(messageType, geoParquetMetadata);
          }
          files.put(file, new FileInfo(file, recordCount, keyValueMetadata, messageType,
              geoParquetMetadata, geoParquetSchema));
        }

        // Verify that the files all have the same schema
        for (int i = 1; i < files.size(); i++) {
          if (!files.get(i).messageType.equals(files.get(i - 1).messageType)) {
            throw new RuntimeException("The files do not have the same schema");
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return files;
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

    private FileStatus file;

    private ParquetReader<GeoParquetGroup> reader;

    public GeoParquetGroupSpliterator(Map<FileStatus, FileInfo> files) {
      this.files = files;
      this.queue = new ArrayBlockingQueue<>(files.keySet().size(), false, files.keySet());

    }

    @Override
    public boolean tryAdvance(Consumer<? super GeoParquetGroup> action) {
      try {
        // Poll the next file
        if (file == null) {
          file = queue.poll();
        }

        // If there are no more files, return false
        if (file == null) {
          return false;
        }

        // Create a new reader if it does not exist
        if (reader == null) {
          reader = createParquetReader(file);
        }

        // Read the next group
        GeoParquetGroup group = reader.read();

        // If the group is null, close the resources and set the variables to null
        if (group == null) {
          reader.close();
          reader = null;
          file = null;

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
        throw new RuntimeException(e);
      }
    }

    @Override
    public Spliterator<GeoParquetGroup> trySplit() {
      // Create a new spliterator by polling the next file
      FileStatus file = queue.poll();

      // If there are no more files, tell the caller that there is nothing to split anymore
      if (file == null) {
        return null;
      }

      // Return a new spliterator with the file
      return new GeoParquetGroupSpliterator(Map.of(file, files.get(file)));
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

  private ParquetReader<GeoParquetGroup> createParquetReader(FileStatus file)
      throws IOException {
    return ParquetReader
        .builder(new GeoParquetGroupReadSupport(), file.getPath())
        .withConf(configuration)
        .build();
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
