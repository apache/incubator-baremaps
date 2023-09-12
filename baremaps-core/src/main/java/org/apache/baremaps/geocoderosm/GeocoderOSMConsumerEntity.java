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

package org.apache.baremaps.geocoderosm;

import java.io.IOException;
import java.util.function.Consumer;
import org.apache.baremaps.openstreetmap.model.Element;
import org.apache.baremaps.openstreetmap.model.Entity;
import org.apache.baremaps.stream.StreamException;
import org.apache.lucene.index.IndexWriter;

public class GeocoderOSMConsumerEntity implements Consumer<Entity> {
  private final IndexWriter indexWriter;
  private final GeocoderOSMDocumentMapper geocoderOsmDocumentMapper =
      new GeocoderOSMDocumentMapper();

  public GeocoderOSMConsumerEntity(IndexWriter indexWriter) {
    this.indexWriter = indexWriter;
  }

  @Override
  public void accept(Entity entity) {
    if (entity instanceof Element element) {
      var document = geocoderOsmDocumentMapper.apply(element);
      try {
        indexWriter.addDocument(document);
      } catch (IOException e) {
        throw new StreamException(e);
      }
    }
  }
}
