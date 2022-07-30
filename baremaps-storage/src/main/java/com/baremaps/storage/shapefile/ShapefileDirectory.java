package com.baremaps.storage.shapefile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.sis.storage.Aggregate;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.Resource;
import org.apache.sis.storage.event.StoreEvent;
import org.apache.sis.storage.event.StoreListener;
import org.opengis.metadata.Metadata;
import org.opengis.util.GenericName;

public class ShapefileDirectory implements Aggregate {

  private final Path directory;

  public ShapefileDirectory(Path directory) {
    this.directory = directory;
  }

  @Override
  public Collection<? extends Resource> components() throws DataStoreException {
    try {
      return Files.list(directory)
          .filter(file -> file.toString().toLowerCase().endsWith(".shp"))
          .map(file -> new ShapefileFile(file))
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new DataStoreException(e);
    }
  }

  @Override
  public Optional<GenericName> getIdentifier() throws DataStoreException {
    return Optional.empty();
  }

  @Override
  public Metadata getMetadata() throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends StoreEvent> void addListener(Class<T> eventType, StoreListener<? super T> listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends StoreEvent> void removeListener(Class<T> eventType, StoreListener<? super T> listener) {
    throw new UnsupportedOperationException();
  }
}
