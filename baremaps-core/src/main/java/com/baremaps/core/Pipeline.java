package com.baremaps.core;

import com.baremaps.core.blob.BlobStore;
import com.baremaps.core.database.repository.PostgresFeatureRepository;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.sql.DataSource;
import org.apache.sis.feature.AbstractFeature;
import org.apache.sis.feature.DefaultFeatureType;
import org.apache.sis.storage.shapefile.InputFeatureStream;
import org.apache.sis.storage.shapefile.ShapeFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pipeline {

  private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);

  private BlobStore blobStore;

  private DataSource dataSource;

  private Path directory;

  private List<Dataset> sources = new ArrayList<>();

  public Pipeline(BlobStore blobStore, DataSource dataSource, Context config, Path directory) {
    this.blobStore = blobStore;
    this.dataSource = dataSource;
    this.directory = directory;
  }

  public void execute() {
    sources.stream().parallel().forEach(this::handle);
  }

  private void handle(Dataset source) {
    URI uri = URI.create(source.getUrl());
    Path dir = directory.resolve(source.getId());
    Path file = dir.resolve(source.getFile());

    // Download and save the source
    switch (source.getArchive()) {
      case "zip":
        downloadZip(uri, dir);
        break;
      default:
        downloadRaw(uri, dir);
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
    try (ZipInputStream zis = new ZipInputStream(blobStore.get(uri).getInputStream())) {
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
    try (InputStream is = blobStore.get(uri).getInputStream()) {
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
      PostgresFeatureRepository repository = new PostgresFeatureRepository(dataSource, featureType);
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

  private void importPbf(Path file) {

  }


}
