package org.apache.sis.storage.shapefile;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.sis.feature.AbstractFeature;
import org.apache.sis.feature.DefaultFeatureType;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.FeatureSet;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.Metadata;
import org.opengis.parameter.ParameterValueGroup;

public class ShapeFileStore extends DataStore implements FeatureSet {

  private final ShapeFile shapeFile;

  public ShapeFileStore(ShapeFile shapeFile) {
    this.shapeFile = shapeFile;
  }

  @Override
  public Optional<Envelope> getEnvelope() throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<ParameterValueGroup> getOpenParameters() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Metadata getMetadata() throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws DataStoreException {
    // The underlying InputStreams are closed by the Streams' onClose handlers
  }

  @Override
  public DefaultFeatureType getType() throws DataStoreException {
    return shapeFile.getFeaturesType();
  }

  @Override
  public Stream<AbstractFeature> features(boolean parallel) throws DataStoreException {
    InputFeatureStream inputFeatureStream = shapeFile.findAll();
    Iterator<AbstractFeature> featureIterator = new FeatureIterator(inputFeatureStream);
    return StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(featureIterator, 0), false)
        .onClose(()-> inputFeatureStream.close());
  }

  public class FeatureIterator implements Iterator<AbstractFeature>{

    private final InputFeatureStream inputFeatureStream;

    private AbstractFeature next;

    public FeatureIterator(InputFeatureStream inputFeatureStream) {
      this.inputFeatureStream = inputFeatureStream;
      try {
        next = inputFeatureStream.readFeature();
      } catch (Exception e) {
        next = null;
        inputFeatureStream.close();
      }
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public AbstractFeature next() {
      if (next == null) {
        throw new NoSuchElementException();
      }
      AbstractFeature feature = next;
      try {
        next = inputFeatureStream.readFeature();
        inputFeatureStream.close();
      } catch (Exception e) {
        next = null;
      } finally {
        return feature;
      }
    }
  }
}
