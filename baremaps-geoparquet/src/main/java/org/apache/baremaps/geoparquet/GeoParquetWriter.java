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

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.ParquetProperties.WriterVersion;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.schema.MessageType;

/**
 * A writer for GeoParquet files that writes GeoParquetGroup instances to a Parquet file.
 */
public class GeoParquetWriter implements AutoCloseable {

  private final ParquetWriter<GeoParquetGroup> parquetWriter;

  /**
   * Constructs a new GeoParquetWriter.
   *
   * @param outputFile the output file
   * @param schema the Parquet schema
   * @param metadata the GeoParquet metadata
   * @throws IOException if an I/O error occurs
   */
  public GeoParquetWriter(Path outputFile, MessageType schema, GeoParquetMetadata metadata)
      throws IOException {
    this.parquetWriter = new ParquetWriter<>(
        outputFile,
        new GeoParquetWriteSupport(schema, metadata),
        CompressionCodecName.UNCOMPRESSED,
        ParquetWriter.DEFAULT_BLOCK_SIZE,
        ParquetWriter.DEFAULT_PAGE_SIZE,
        ParquetWriter.DEFAULT_PAGE_SIZE,
        ParquetWriter.DEFAULT_IS_DICTIONARY_ENABLED,
        ParquetWriter.DEFAULT_IS_VALIDATING_ENABLED,
        WriterVersion.PARQUET_2_0,
        new Configuration());
  }

  /**
   * Writes a GeoParquetGroup to the Parquet file.
   *
   * @param group the GeoParquetGroup to write
   * @throws IOException if an I/O error occurs
   */
  public void write(GeoParquetGroup group) throws IOException {
    parquetWriter.write(group);
  }

  /**
   * Closes the writer and releases any system resources associated with it.
   *
   * @throws IOException if an I/O error occurs
   */
  public void close() throws IOException {
    parquetWriter.close();
  }
}
