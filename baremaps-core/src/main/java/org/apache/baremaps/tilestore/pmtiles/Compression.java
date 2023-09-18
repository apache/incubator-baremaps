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

package org.apache.baremaps.tilestore.pmtiles;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

enum Compression {
  Unknown,
  None,
  Gzip,
  Brotli,
  Zstd;

  InputStream decompress(InputStream inputStream) throws IOException {
    return switch (this) {
      case None -> inputStream;
      case Gzip -> decompressGzip(inputStream);
      case Brotli -> decompressBrotli(inputStream);
      case Zstd -> decompressZstd(inputStream);
      default -> throw new RuntimeException("Unknown compression");
    };
  }

  static InputStream decompressGzip(InputStream inputStream) throws IOException {
    return new GZIPInputStream(inputStream);
  }

  static InputStream decompressBrotli(InputStream buffer) {
    throw new RuntimeException("Brotli compression not implemented");
  }

  static InputStream decompressZstd(InputStream buffer) {
    throw new RuntimeException("Zstd compression not implemented");
  }

  OutputStream compress(OutputStream outputStream) throws IOException {
    return switch (this) {
      case None -> outputStream;
      case Gzip -> compressGzip(outputStream);
      case Brotli -> compressBrotli(outputStream);
      case Zstd -> compressZstd(outputStream);
      default -> throw new RuntimeException("Unknown compression");
    };
  }

  static OutputStream compressGzip(OutputStream outputStream) throws IOException {
    return new GZIPOutputStream(outputStream);
  }

  static OutputStream compressBrotli(OutputStream outputStream) {
    throw new RuntimeException("Brotli compression not implemented");
  }

  static OutputStream compressZstd(OutputStream outputStream) {
    throw new RuntimeException("Zstd compression not implemented");
  }
}
