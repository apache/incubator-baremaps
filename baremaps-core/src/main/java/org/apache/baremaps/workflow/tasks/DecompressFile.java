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

package org.apache.baremaps.workflow.tasks;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipFile;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decompresses a file based on a given compression format. The supported formats are zip, targz,
 * tarbz2, gzip and bzip2.
 */
@JsonTypeName("DecompressFile")
public class DecompressFile implements Task {

  private static final Logger logger = LoggerFactory.getLogger(DecompressFile.class);

  /**
   * The compression format.
   */
  public enum Compression {
    zip,
    targz,
    tarbz2,
    gzip,
    bzip2;
  }

  private Path source;
  private Path target;
  private Compression compression;

  /**
   * Constructs a {@code DecompressFile}.
   *
   * @param source the source file
   * @param target the target file
   * @param compression the compression format (zip, targz, tarbz2, gzip or bzip2)
   */
  public DecompressFile(Path source, Path target, Compression compression) {
    this.source = source;
    this.target = target;
    this.compression = compression;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(WorkflowContext context) throws Exception {
    var sourcePath = source.toAbsolutePath();
    var targetPath = target.toAbsolutePath();
    switch (compression) {
      case zip -> decompressZip(sourcePath, targetPath);
      case targz -> decompressTarGz(sourcePath, targetPath);
      case tarbz2 -> decompressTarBz2(sourcePath, targetPath);
      case gzip -> decompressGzip(sourcePath, targetPath);
      case bzip2 -> decompressBzip2(sourcePath, targetPath);
    }
  }

  /**
   * Decompresses a bzip2 file.
   * 
   * @param source the source file
   * @param target the target file
   * @throws IOException if an I/O error occurs
   */
  protected static void decompressBzip2(Path source, Path target) throws IOException {
    try (var bufferedInputStream = new BufferedInputStream(Files.newInputStream(source));
        var bzip2InputStream = new BZip2CompressorInputStream(bufferedInputStream)) {
      Files.copy(bzip2InputStream, target, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * Decompresses a gzip file.
   * 
   * @param source the source file
   * @param target the target file
   * @throws IOException if an I/O error occurs
   */
  protected static void decompressGzip(Path source, Path target) throws IOException {
    try (var zis = new GZIPInputStream(new BufferedInputStream(Files.newInputStream(source)))) {
      Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * Decompresses a tar.gz file.
   * 
   * @param source the source file
   * @param target the target file
   * @throws IOException if an I/O error occurs
   */
  protected static void decompressTarGz(Path source, Path target) throws IOException {
    try (var bufferedInputStream = new BufferedInputStream(Files.newInputStream(source));
        var gzipInputStream = new GZIPInputStream(bufferedInputStream);
        var tarInputStream = new TarArchiveInputStream(gzipInputStream)) {
      TarArchiveEntry entry;
      while ((entry = (TarArchiveEntry) tarInputStream.getNextEntry()) != null) {
        var path = target.resolve(entry.getName());
        if (entry.isDirectory()) {
          Files.createDirectories(path);
        } else {
          Files.createDirectories(path.getParent());
          try (BufferedOutputStream outputStream =
              new BufferedOutputStream(Files.newOutputStream(path))) {
            tarInputStream.transferTo(outputStream);
          }
        }
      }
    }
  }

  /**
   * Decompresses a tar.bz2 file.
   * 
   * @param source the source file
   * @param target the target file
   * @throws IOException if an I/O error occurs
   */
  protected static void decompressTarBz2(Path source, Path target) throws IOException {
    try (var bufferedInputStream = new BufferedInputStream(Files.newInputStream(source));
        var bzip2InputStream = new BZip2CompressorInputStream(bufferedInputStream);
        var tarInputStream = new TarArchiveInputStream(bzip2InputStream)) {
      TarArchiveEntry entry;
      while ((entry = (TarArchiveEntry) tarInputStream.getNextEntry()) != null) {
        var path = target.resolve(entry.getName());
        if (entry.isDirectory()) {
          Files.createDirectories(path);
        } else {
          Files.createDirectories(path.getParent());
          try (BufferedOutputStream outputStream =
              new BufferedOutputStream(Files.newOutputStream(path))) {
            tarInputStream.transferTo(outputStream);
          }
        }
      }
    }
  }

  /**
   * Decompresses a zip file.
   * 
   * @param source the source file
   * @param target the target file
   * @throws IOException if an I/O error occurs
   */
  protected static void decompressZip(Path source, Path target) throws IOException {
    try (var zipFile = new ZipFile(source.toFile())) {
      var entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        var entry = entries.nextElement();
        var path = target.resolve(entry.getName());
        Files.createDirectories(path.getParent());
        Files.write(path, new byte[] {}, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING);
        try (var input = new BufferedInputStream(zipFile.getInputStream(entry));
            var output = new BufferedOutputStream(new FileOutputStream(path.toFile()))) {
          input.transferTo(output);
        }
      }
    }
  }
}
