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

  private MessageType schema;

  private Map<String, String> keyValueMetadata;

  private GeoParquetMetadata metadata;

  private GeoParquetGroup.Schema geoParquetSchema;

  public GeoParquetReader(URI uri) {
    this(uri, createConfiguration());
  }

  public GeoParquetReader(URI uri, Configuration configuration) {
    this.uri = uri;
    this.configuration = configuration;
  }

  public MessageType getParquetSchema() throws IOException, URISyntaxException {
    if (schema == null) {
      init();
    }
    return schema;
  }

  public GeoParquetMetadata getGeoParquetMetadata() throws IOException, URISyntaxException {
    if (schema == null) {
      init();
    }
    return metadata;
  }

  public Schema getGeoParquetSchema() throws IOException, URISyntaxException {
    if (schema == null) {
      init();
    }
    return geoParquetSchema;
  }

  private void init() throws URISyntaxException {
    try {
      Path globPath = new Path(uri.getPath());
      URI rootUri = getRootUri(uri);
      FileSystem fileSystem = FileSystem.get(rootUri, configuration);
      List<FileStatus> files = Arrays.asList(fileSystem.globStatus(globPath));
      FileStatus file = files.get(0);
      ParquetFileReader reader = ParquetFileReader.open(configuration, file.getPath());
      schema = reader.getFileMetaData().getSchema();
      keyValueMetadata = reader.getFileMetaData().getKeyValueMetaData();
      if (keyValueMetadata.containsKey("geo")) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        metadata = objectMapper.readValue(keyValueMetadata.get("geo"), GeoParquetMetadata.class);
        geoParquetSchema = GeoParquetGroupFactory.createGeoParquetSchema(schema, metadata);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Stream<GeoParquetGroup> readParallel() throws IOException, URISyntaxException {
    Path globPath = new Path(uri.getPath());
    URI rootUri = getRootUri(uri);
    FileSystem fileSystem = FileSystem.get(rootUri, configuration);
    List<FileStatus> files = Arrays.asList(fileSystem.globStatus(globPath));
    return StreamSupport.stream(
        new GeoParquetGroupSpliterator(files),
        true);
  }

  public Stream<GeoParquetGroup> read() throws IOException, URISyntaxException {
    return readParallel().sequential();
  }

  public class GeoParquetGroupSpliterator implements Spliterator<GeoParquetGroup> {

    private final Queue<FileStatus> files;

    private FileStatus file;

    private ParquetReader<GeoParquetGroup> reader;

    public GeoParquetGroupSpliterator(List<FileStatus> files) {
      this.files = new ArrayBlockingQueue<>(files.size(), false, files);
    }

    @Override
    public boolean tryAdvance(Consumer<? super GeoParquetGroup> action) {
      try {
        // Poll the next file
        if (file == null) {
          file = files.poll();
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
      FileStatus file = files.poll();

      // If there are no more files, tell the caller that there is nothing to split anymore
      if (file == null) {
        return null;
      }

      // Return a new spliterator with the file
      return new GeoParquetGroupSpliterator(Collections.singletonList(file));
    }

    @Override
    public long estimateSize() {
      // The size is unknown
      return Long.MAX_VALUE;
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
