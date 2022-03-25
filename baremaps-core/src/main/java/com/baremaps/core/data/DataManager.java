package com.baremaps.core.data;

import com.baremaps.core.blob.BlobStore;
import com.baremaps.core.blob.BlobStoreException;
import com.baremaps.core.database.repository.PostgresFeatureRepository;
import com.baremaps.core.database.repository.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.sql.DataSource;
import org.apache.sis.feature.AbstractFeature;
import org.apache.sis.feature.DefaultFeatureType;
import org.apache.sis.internal.shapefile.jdbc.DBase3FieldDescriptor;
import org.apache.sis.storage.DataStoreClosedException;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.shapefile.DataStoreQueryException;
import org.apache.sis.storage.shapefile.DataStoreQueryResultException;
import org.apache.sis.storage.shapefile.DbaseFileNotFoundException;
import org.apache.sis.storage.shapefile.InputFeatureStream;
import org.apache.sis.storage.shapefile.InvalidDbaseFileFormatException;
import org.apache.sis.storage.shapefile.InvalidShapefileFormatException;
import org.apache.sis.storage.shapefile.ShapeFile;
import org.apache.sis.storage.shapefile.ShapefileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataManager {

  private static final Logger logger = LoggerFactory.getLogger(DataManager.class);

  private final BlobStore blobStore;

  private final DataSource dataSource;

  private final Config config;

  private final Path root;

  public DataManager(BlobStore blobStore, DataSource dataSource, Config config, Path directory) {
    this.blobStore = blobStore;
    this.dataSource = dataSource;
    this.config = config;
    this.root = directory;
  }

  public void execute() {
    config.getSources().stream().parallel().forEach(this::handle);
  }

  private void handle(Source source) {
    URI uri = URI.create(source.getUrl());
    Path directory = root.resolve(source.getId());
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
        // TODO: importPbf(file);
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
    } catch (IOException exception) {
      exception.printStackTrace();
    } catch (BlobStoreException e) {
      e.printStackTrace();
    }
  }

  private void downloadRaw(URI uri, Path path) {
    Path file = path.resolve(Paths.get(uri.getPath()).getFileName());
    try (InputStream is = blobStore.get(uri).getInputStream()) {
      Files.createDirectories(file.getParent());
      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException exception) {
      exception.printStackTrace();
    } catch (BlobStoreException e) {
      e.printStackTrace();
    }
  }

  private void importShp(Path file) {
    ShapeFile shp = new ShapeFile(file.toAbsolutePath().toString());

    try (InputFeatureStream is = shp.findAll()) {
      DefaultFeatureType featureType = is.getFeaturesType();
      List<DBase3FieldDescriptor> descriptors = is.getDatabaseFieldsDescriptors();

      PostgresFeatureRepository repository = new PostgresFeatureRepository(dataSource, featureType, descriptors);
      repository.create();
      AbstractFeature feature = is.readFeature();
      while (feature != null) {
        feature = is.readFeature();
        repository.insert(feature);
      }
    } catch (DataStoreClosedException e) {
      e.printStackTrace();
    } catch (DataStoreQueryResultException e) {
      e.printStackTrace();
    } catch (DataStoreQueryException e) {
      e.printStackTrace();
    } catch (InvalidDbaseFileFormatException e) {
      e.printStackTrace();
    } catch (ShapefileNotFoundException e) {
      e.printStackTrace();
    } catch (DbaseFileNotFoundException e) {
      e.printStackTrace();
    } catch (InvalidShapefileFormatException e) {
      e.printStackTrace();
    } catch (DataStoreException e) {
      e.printStackTrace();
    } catch (RepositoryException e) {
      e.printStackTrace();
    }
  }

}
