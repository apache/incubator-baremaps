/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.pipeline;

import com.baremaps.pipeline.config.Config;
import com.baremaps.pipeline.config.Source;
import com.baremaps.pipeline.database.repository.PostgresFeatureRepository;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.sis.feature.AbstractFeature;
import org.apache.sis.feature.DefaultFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pipeline {

  private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);

  private Context context;

  private Config config;

  public Pipeline(Context context, Config config) {
    this.context = context;
    this.config = config;
  }

  public void execute() {
    CompletableFuture[] futures =
        config.getSources().stream().map(this::handle).toArray(size -> new CompletableFuture[size]);
    CompletableFuture.allOf(futures).join();
  }

  public CompletableFuture<Void> handle(Source source) {
    CompletableFuture<Void> steps = CompletableFuture.completedFuture(null);

    // download url
    Path sourceDirectory = context.directory().resolve(source.getId()).resolve("file");
    Path downloadFile = sourceDirectory;

    if (Files.exists(downloadFile)) {
      steps = steps.thenRunAsync(() -> download(source));
    }

    // expand archive
    if ("zip".equals(source.getArchive())) {
      steps = steps.thenRunAsync(() -> unzip(source));
    }

    // import shape file
    if ("shp".equals(source.getFormat())) {
      steps = steps.thenRunAsync(() -> importShp(source));
    }

    return steps;
  }

  private void download(Source source) {
    try (InputStream inputStream =
        context.blobStore().get(URI.create(source.getUrl())).getInputStream()) {
      Path sourceDirectory = Files.createDirectories(context.directory().resolve(source.getId()));
      Path downloadFile = Files.createFile(sourceDirectory.resolve("file"));
      Files.copy(inputStream, downloadFile, StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  private void unzip(Source source) {
    Path sourceDirectory = context.directory().resolve(source.getId());
    Path downloadFile = sourceDirectory.resolve("file");
    try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(downloadFile)))) {
      Path archiveDirectory = Files.createDirectories(sourceDirectory.resolve("archive"));
      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        Path file = Files.createDirectories(archiveDirectory.resolve(ze.getName()));
        Files.copy(zis, file, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  private void importShp(Source source) {

  }
}
