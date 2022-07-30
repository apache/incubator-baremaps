package com.baremaps.storage.shapefile;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.event.StoreEvent;
import org.apache.sis.storage.event.StoreListener;
import org.apache.sis.storage.shapefile.InputFeatureStream;
import org.apache.sis.storage.shapefile.ShapeFile;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureType;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.Metadata;
import org.opengis.util.GenericName;

public class ShapefileFile implements FeatureSet, AutoCloseable {

  private final ShapeFile shapeFile;

  public ShapefileFile(Path file) {
    this.shapeFile = new ShapeFile(file.toString());
  }

  @Override
  public FeatureType getType() throws DataStoreException {
    return shapeFile.findAll().getFeaturesType();
  }

  @Override
  public Stream<Feature> features(boolean parallel) throws DataStoreException {
    return StreamSupport.stream(new FeatureSpliterator(shapeFile.findAll()), false);
  }

  @Override
  public Optional<Envelope> getEnvelope() throws DataStoreException {
    return Optional.empty();
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

  @Override
  public void close() throws Exception {

  }

  private class FeatureSpliterator implements Spliterator<Feature> {

    private final InputFeatureStream inputFeatureStream;

    public FeatureSpliterator(InputFeatureStream inputFeatureStream) {
      this.inputFeatureStream = inputFeatureStream;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Feature> action) {
      try {
        var feature = inputFeatureStream.readFeature();
        if (feature != null) {
          action.accept(feature);
          return true;
        } else {
          return false;
        }
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }
    }

    @Override
    public Spliterator<Feature> trySplit() {
      return null;
    }

    @Override
    public long estimateSize() {
      return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
      return 0;
    }
  }

}
