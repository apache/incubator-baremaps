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

package org.apache.baremaps.storage.postgres;



import java.io.IOException;
import java.util.stream.Stream;
import org.apache.baremaps.feature.Feature;
import org.apache.baremaps.feature.FeatureType;
import org.apache.baremaps.feature.WritableFeatureSet;

public class PostgresTable implements WritableFeatureSet {

  private final FeatureType featureType;

  public PostgresTable(FeatureType featureType) {
    this.featureType = featureType;
  }

  @Override
  public FeatureType getType() throws IOException {
    return featureType;
  }

  @Override
  public void write(Stream<Feature> features) throws IOException {
    throw new UnsupportedOperationException();
  }
}
