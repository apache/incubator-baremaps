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
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.filter2.compat.FilterCompat;
import org.apache.parquet.filter2.compat.FilterCompat.Filter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.FileMetaData;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.RecordReader;
import org.apache.parquet.schema.MessageType;
import org.locationtech.jts.geom.Envelope;

class GeoParquetSpliterator implements Spliterator<GeoParquetGroup> {

  private final List<FileStatus> files;
  private final Configuration configuration;

  private ParquetFileReader fileReader;
  private int fileStartIndex;
  private int fileEndIndex;
  private MessageType schema;
  private GeoParquetMetadata metadata;
  private MessageColumnIO columnIO;
  private RecordReader<GeoParquetGroup> recordReader;
  private int currentRowGroup;
  private long rowsReadInGroup;
  private long rowsInCurrentGroup;

  GeoParquetSpliterator(
      List<FileStatus> files,
      Configuration configuration,
      int fileStartIndex,
      int fileEndIndex) {
    this.files = files;
    this.configuration = configuration;
    this.fileStartIndex = fileStartIndex;
    this.fileEndIndex = fileEndIndex;
    setupReaderForNextFile();
  }

  private void setupReaderForNextFile() {
    closeCurrentReader();

    if (fileStartIndex >= fileEndIndex) {
      fileReader = null;
      return;
    }

    FileStatus fileStatus = files.get(fileStartIndex++);
    try {
      InputFile inputFile = HadoopInputFile.fromPath(fileStatus.getPath(), configuration);
      fileReader = ParquetFileReader.open(inputFile);

      FileMetaData fileMetaData = fileReader.getFooter().getFileMetaData();

      schema = fileMetaData.getSchema();
      metadata = new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .readValue(fileMetaData.getKeyValueMetaData().get("geo"), GeoParquetMetadata.class);

      columnIO = new ColumnIOFactory().getColumnIO(schema);
      currentRowGroup = 0;
      rowsReadInGroup = 0;
      rowsInCurrentGroup = 0;
      advanceToNextRowGroup();
    } catch (IOException e) {
      throw new GeoParquetException("Failed to create reader for " + fileStatus, e);
    }
  }

  private void advanceToNextRowGroup() throws IOException {
    if (currentRowGroup >= fileReader.getRowGroups().size()) {
      setupReaderForNextFile();
      return;
    }

    PageReadStore pages = fileReader.readNextFilteredRowGroup();
    if (pages == null) {
      setupReaderForNextFile();
      return;
    }

    rowsInCurrentGroup = pages.getRowCount();
    rowsReadInGroup = 0;

    GeoParquetGroupRecordMaterializer materializer =
        new GeoParquetGroupRecordMaterializer(schema, metadata);
    recordReader = columnIO.getRecordReader(pages, materializer, FilterCompat.NOOP);
    currentRowGroup++;
  }

  @Override
  public boolean tryAdvance(Consumer<? super GeoParquetGroup> action) {
    try {
      while (true) {
        if (fileReader == null) {
          return false;
        }

        if (rowsReadInGroup >= rowsInCurrentGroup) {
          advanceToNextRowGroup();
          continue;
        }

        GeoParquetGroup group = recordReader.read();
        if (group == null) {
          // Should not happen unless there is an error
          throw new GeoParquetException("Unexpected null group read from recordReader.");
        }

        rowsReadInGroup++;
        action.accept(group);
        return true;
      }
    } catch (IOException e) {
      closeCurrentReader();
      throw new GeoParquetException("IOException caught while trying to read the next record.", e);
    }
  }

  private void closeCurrentReader() {
    if (fileReader != null) {
      try {
        fileReader.close();
      } catch (IOException e) {
        throw new GeoParquetException("Failed to close ParquetFileReader.", e);
      } finally {
        fileReader = null;
      }
    }
  }

  @Override
  public Spliterator<GeoParquetGroup> trySplit() {
    int remainingFiles = fileEndIndex - fileStartIndex;
    if (remainingFiles <= 1) {
      return null;
    }
    int mid = fileStartIndex + remainingFiles / 2;
    GeoParquetSpliterator split = new GeoParquetSpliterator(files, configuration, mid, fileEndIndex);
    this.fileEndIndex = mid;
    return split;
  }

  @Override
  public long estimateSize() {
    // Return Long.MAX_VALUE as the actual number of elements is unknown
    return Long.MAX_VALUE;
  }

  @Override
  public int characteristics() {
    return NONNULL | IMMUTABLE;
  }
}
