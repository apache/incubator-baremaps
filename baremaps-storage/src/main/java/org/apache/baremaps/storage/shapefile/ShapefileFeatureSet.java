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

package org.apache.baremaps.storage.shapefile;



import java.nio.file.Path;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.baremaps.storage.shapefile.internal.InputFeatureStream;
import org.apache.baremaps.storage.shapefile.internal.ShapefileReader;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.event.StoreEvent;
import org.apache.sis.storage.event.StoreListener;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureType;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.Metadata;
import org.opengis.util.GenericName;

public class ShapefileFeatureSet implements FeatureSet, AutoCloseable {

  private final ShapefileReader shapeFile;

  public ShapefileFeatureSet(Path file) {
    this.shapeFile = new ShapefileReader(file.toString());
  }

  @Override
  public FeatureType getType() throws DataStoreException {
    try (var input = shapeFile.read()) {
      return input.getFeaturesType();
    } catch (Exception e) {
      throw new DataStoreException(e);
    }
  }

  @Override
  public Stream<Feature> features(boolean parallel) throws DataStoreException {
    try {
      var input = shapeFile.read();
      return StreamSupport.stream(new FeatureSpliterator(shapeFile.read()), false)
          .onClose(() -> input.close());
    } catch (Exception e) {
      throw new DataStoreException(e);
    }
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
  public <T extends StoreEvent> void addListener(Class<T> eventType,
      StoreListener<? super T> listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends StoreEvent> void removeListener(Class<T> eventType,
      StoreListener<? super T> listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws Exception {}

  static class FeatureSpliterator implements Spliterator<Feature> {

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
