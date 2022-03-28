/*
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

package com.baremaps.core;

import com.baremaps.core.database.repository.PostgresFeatureRepository;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.sis.feature.AbstractFeature;
import org.apache.sis.feature.DefaultFeatureType;
import org.apache.sis.storage.shapefile.InputFeatureStream;
import org.apache.sis.storage.shapefile.ShapeFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pipeline {

  private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);

  private Context context;

  private List<Source> sources = new ArrayList<>();

  public Pipeline(Context context) {
    this.context = context;
  }

  public void execute() {
    sources.stream().parallel().forEach(this::handle);
  }

  private void handle(Source source) {
    URI uri = URI.create(source.getUrl());
    Path directory = context.directory().resolve(source.getId());
    Path file = directory.resolve(source.getFile());

    // Download and save the source
    switch (source.getArchive()) {
      case "zip":
        downloadZip(uri, directory);
        break;
      default:
        downloadRaw(uri, directory);
        break;
    }

    // Import the source in the database
    switch (source.getFormat()) {
      case "pbf":
        importPbf(file);
        break;
      case "shp":
        importShp(file);
        break;
      case "sqlite":
        // TODO: importSqlite(file);
        break;
      default:
        break;
    }

    // Simplify the source at multiple zoom levels

    // Index the source
  }

  private void downloadZip(URI uri, Path path) {
    try (ZipInputStream zis = new ZipInputStream(context.blobStore().get(uri).getInputStream())) {
      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        Path file = path.resolve(ze.getName());
        Files.createDirectories(file.getParent());
        Files.copy(zis, file, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  private void downloadRaw(URI uri, Path path) {
    Path file = path.resolve(Paths.get(uri.getPath()).getFileName());
    try (InputStream is = context.blobStore().get(uri).getInputStream()) {
      Files.createDirectories(file.getParent());
      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  private void importShp(Path file) {
    ShapeFile shp = new ShapeFile(file.toAbsolutePath().toString());
    try (InputFeatureStream is = shp.findAll()) {
      DefaultFeatureType featureType = is.getFeaturesType();
      PostgresFeatureRepository repository =
          new PostgresFeatureRepository(context.dataSource(), featureType);
      repository.create();
      AbstractFeature feature = is.readFeature();
      while (feature != null) {
        feature = is.readFeature();
        repository.insert(feature);
      }
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  private void importPbf(Path file) {}
}
