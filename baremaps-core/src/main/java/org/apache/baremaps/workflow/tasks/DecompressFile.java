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

@JsonTypeName("DecompressFile")
public class DecompressFile implements Task {

  private static final Logger logger = LoggerFactory.getLogger(DecompressFile.class);

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

  public DecompressFile() {}

  public DecompressFile(Path source, Path target, Compression compression) {
    this.source = source;
    this.target = target;
    this.compression = compression;
  }

  public Path getSource() {
    return source;
  }

  public void setSource(Path source) {
    this.source = source;
  }

  public Path getTarget() {
    return target;
  }

  public void setTarget(Path target) {
    this.target = target;
  }

  public Compression getCompression() {
    return compression;
  }

  public void setCompression(Compression compression) {
    this.compression = compression;
  }

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

  public static void decompressBzip2(Path sourcePath, Path targetPath) throws IOException {
    try (var bufferedInputStream = new BufferedInputStream(Files.newInputStream(sourcePath));
        var bzip2InputStream = new BZip2CompressorInputStream(bufferedInputStream)) {
      Files.copy(bzip2InputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  public static void decompressGzip(Path sourcePath, Path targetPath) throws IOException {
    try (var zis = new GZIPInputStream(new BufferedInputStream(Files.newInputStream(sourcePath)))) {
      Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  public static void decompressTarGz(Path sourcePath, Path targetPath) throws IOException {
    try (var bufferedInputStream = new BufferedInputStream(Files.newInputStream(sourcePath));
        var gzipInputStream = new GZIPInputStream(bufferedInputStream);
        var tarInputStream = new TarArchiveInputStream(gzipInputStream)) {
      TarArchiveEntry entry;
      while ((entry = (TarArchiveEntry) tarInputStream.getNextEntry()) != null) {
        var path = targetPath.resolve(entry.getName());
        if (entry.isDirectory()) {
          Files.createDirectories(path);
        } else {
          Files.createDirectories(path.getParent());
          try (BufferedOutputStream outputStream =
              new BufferedOutputStream(Files.newOutputStream(path))) {
            int bytesRead;
            byte[] buffer = new byte[4096];
            while ((bytesRead = tarInputStream.read(buffer)) != -1) {
              outputStream.write(buffer, 0, bytesRead);
            }
          }
        }
      }
    }
  }

  public static void decompressTarBz2(Path sourcePath, Path targetPath) throws IOException {
    try (var bufferedInputStream = new BufferedInputStream(Files.newInputStream(sourcePath));
        var bzip2InputStream = new BZip2CompressorInputStream(bufferedInputStream);
        var tarInputStream = new TarArchiveInputStream(bzip2InputStream)) {
      TarArchiveEntry entry;
      while ((entry = (TarArchiveEntry) tarInputStream.getNextEntry()) != null) {
        var path = targetPath.resolve(entry.getName());
        if (entry.isDirectory()) {
          Files.createDirectories(path);
        } else {
          Files.createDirectories(path.getParent());
          try (BufferedOutputStream outputStream =
              new BufferedOutputStream(Files.newOutputStream(path))) {
            int bytesRead;
            byte[] buffer = new byte[4096];
            while ((bytesRead = tarInputStream.read(buffer)) != -1) {
              outputStream.write(buffer, 0, bytesRead);
            }
          }
        }
      }
    }
  }

  public static void decompressZip(Path sourcePath, Path targetPath) throws IOException {
    try (var zipFile = new ZipFile(sourcePath.toFile())) {
      var entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        var entry = entries.nextElement();
        var path = targetPath.resolve(entry.getName());
        Files.createDirectories(path.getParent());
        Files.write(path, new byte[] {}, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING);
        try (var input = new BufferedInputStream(zipFile.getInputStream(entry));
            var output = new BufferedOutputStream(new FileOutputStream(path.toFile()))) {
          int nBytes = -1;
          byte[] buffer = new byte[4096];
          while ((nBytes = input.read(buffer)) > 0) {
            output.write(buffer, 0, nBytes);
          }
        }
      }
    }
  }
}
